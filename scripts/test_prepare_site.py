import importlib.util
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("prepare_site", Path(__file__).with_name("prepare-site.py"))
site = importlib.util.module_from_spec(spec)
spec.loader.exec_module(site)


class PrepareSiteTest(unittest.TestCase):
    def test_menu_keeps_latest_patch_per_minor_and_only_next_snapshot(self):
        tags = ["v0.7.0", "v0.7.1", "v0.9.0", "v0.10.0", "v0.10.2",
                "v0.10.10", "v0.11.0-rc.1", "v0.8.0"]
        self.assertEqual(site.version_menu(tags), {
            "supported": ["0.10.10", "0.9.0"],
            "upcoming": ["0.11.0-SNAPSHOT"],
            "old": ["0.8.0", "0.7.1"],
        })

    def test_menu_distinguishes_major_versions(self):
        self.assertEqual(site.version_menu(["v1.0.1", "v0.0.2", "v1.0.0"]), {
            "supported": ["1.0.1", "0.0.2"],
            "upcoming": ["1.1.0-SNAPSHOT"],
            "old": [],
        })

    def test_legacy_include_uses_the_java_file_from_the_same_tag(self):
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory)
            java = source / "grooves-gorm/src/main/java/com/github/rahulsom/grooves/grails/GormQuerySupport.java"
            java.parent.mkdir(parents=True)
            java.touch()
            docs = source / "docs"
            docs.mkdir()
            page = docs / "queries.adoc"
            page.write_text("include::{sourcedir}/" + str(java.relative_to(source)).replace("/java/", "/groovy/") + "[tags=documented]")
            site.repair_legacy_includes(source, docs)
            self.assertIn(str(java.relative_to(source)), page.read_text())

    def test_highlighted_include_errors_stop_publication(self):
        with tempfile.TemporaryDirectory() as directory:
            rendered = Path(directory)
            (rendered / "index.html").write_text('<span>Unresolved</span> <span>directive</span> in examples')
            with self.assertRaisesRegex(ValueError, "Unresolved source includes"):
                site.validate_manual(rendered, "v0.6.0")

    def test_release_order_includes_prereleases_and_ignores_unrelated_tags(self):
        with patch.object(site, "run") as run:
            run.return_value.stdout = "v0.9.0\nv0.10.0-rc.1\nv0.10.0\nother\nv0.7.1\n"
            self.assertEqual(site.release_tags(), ["v0.10.0", "v0.10.0-rc.1", "v0.9.0", "v0.7.1"])

    def test_favicon_backfill_preserves_content_and_is_idempotent(self):
        with tempfile.TemporaryDirectory() as directory:
            pages = Path(directory)
            manual = pages / "manual/0.7.0/index.html"
            manual.parent.mkdir(parents=True)
            manual.write_text("<html><head><title>Old manual</title></head><body>Examples</body></html>")
            (pages / "manual/current").symlink_to("0.7.0", target_is_directory=True)
            site.add_favicons(pages)
            once = manual.read_text()
            site.add_favicons(pages)
            self.assertEqual(manual.read_text(), once)
            self.assertIn(site.ICON, once)
            self.assertIn("<body>Examples</body>", once)

    def test_existing_icon_is_preserved(self):
        with tempfile.TemporaryDirectory() as directory:
            pages = Path(directory)
            manual = pages / "manual/1.0.0/index.html"
            manual.parent.mkdir(parents=True)
            html = '<head><link rel="icon" href="custom.ico"></head>'
            manual.write_text(html)
            site.add_favicons(pages)
            self.assertEqual(manual.read_text(), html)


if __name__ == "__main__":
    unittest.main()
