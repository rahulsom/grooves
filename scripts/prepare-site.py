#!/usr/bin/env python3
"""Backfill tagged manuals into a gh-pages checkout before publishing it."""

import argparse
import html
import json
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import tempfile


ROOT = Path(__file__).resolve().parents[1]
TAG = re.compile(r"v(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z.-]+))?$")
ICON = '<link rel="icon" type="image/svg+xml" href="/grooves/favicon.svg">'


def run(*args, **kwargs):
    kwargs.setdefault("cwd", ROOT)
    return subprocess.run(args, check=True, **kwargs)


def release_tags():
    tags = run("git", "tag", capture_output=True, text=True).stdout.splitlines()
    return sorted(
        (tag for tag in tags if TAG.fullmatch(tag)),
        key=lambda tag: (*map(int, TAG.fullmatch(tag).groups()[:3]),
                         TAG.fullmatch(tag)[4] is None, tag),
        reverse=True,
    )


def add_favicons(pages):
    # Patch preserved manuals too, without rebuilding their historical content.
    for page in (pages / "manual").rglob("*.html"):
        if page.is_symlink():
            continue
        html = page.read_text()
        if not re.search(r'<link\b[^>]*\brel=["\'](?:shortcut )?icon["\']', html, re.I):
            html, count = re.subn(r"</head\s*>", ICON + "\n</head>", html, count=1, flags=re.I)
            if count != 1:
                raise ValueError(f"Missing HTML head: {page}")
            page.write_text(html)


def version_menu(tags):
    releases = sorted(
        (tuple(map(int, TAG.fullmatch(tag).groups()[:3]))
         for tag in tags if TAG.fullmatch(tag)[4] is None),
        reverse=True,
    )
    if not releases:
        raise ValueError("At least one stable release is required for the version menu")
    latest_by_minor = {}
    for major, minor, patch in releases:
        latest_by_minor.setdefault((major, minor), f"{major}.{minor}.{patch}")
    stable = list(latest_by_minor.values())
    major, minor, _ = releases[0]
    return {
        "supported": stable[:2],
        "upcoming": [f"{major}.{minor + 1}.0-SNAPSHOT"],
        "old": stable[2:],
    }


def repair_legacy_includes(source, docs):
    # 0.6.0 and 0.6.1 release candidates pointed at a Java file in the Groovy tree.
    wrong = "grooves-gorm/src/main/groovy/com/github/rahulsom/grooves/grails/GormQuerySupport.java"
    correct = wrong.replace("/groovy/", "/java/")
    if (source / correct).is_file() and not (source / wrong).exists():
        for page in docs.rglob("*.adoc"):
            text = page.read_text()
            if wrong in text:
                page.write_text(text.replace(wrong, correct))


def validate_manual(rendered, version):
    # Syntax highlighting splits error messages across spans.
    content = html.unescape(re.sub(r"<[^>]+>", "", (rendered / "index.html").read_text()))
    if "Unresolved directive" in content or "include file not found" in content:
        raise ValueError(f"Unresolved source includes in {version}")


def prepare(pages):
    tags = release_tags()
    if not tags:
        raise ValueError("No release tags found; fetch the complete tag history first")
    versions = version_menu(tags)
    pages.mkdir(parents=True, exist_ok=True)
    for tag in tags:
        version = tag[1:]
        destination = pages / "manual" / version
        if (destination / "index.html").is_file():
            continue
        print(f"Building missing manual for {tag}", flush=True)
        with tempfile.TemporaryDirectory(prefix="grooves-manual-") as directory:
            source = Path(directory)
            archive = source / "source.tar"
            with archive.open("wb") as stream:
                run("git", "archive", "--format=tar", tag, stdout=stream)
            with tarfile.open(archive) as contents:
                contents.extractall(source, filter="data")
            candidates = sorted(source.glob("*/src/docs/asciidoc/index.adoc"))
            preferred = source / "grooves-docs/src/docs/asciidoc/index.adoc"
            index = preferred if preferred.is_file() else next(iter(candidates), None)
            if index is None:
                raise ValueError(f"No manual source found for {tag}")
            repair_legacy_includes(source, index.parent)
            output = source / "rendered"
            run(str(ROOT / "gradlew"), ":grooves-docs:asciidoctor",
                f"-PmanualRoot={source}", f"-PmanualSource={index.parent}",
                f"-PmanualVersion={version}", f"-PmanualOutput={output}")
            # The Gradle plugin normally adds the backend name to outputDir.
            rendered = output / "html5" if (output / "html5/index.html").is_file() else output
            validate_manual(rendered, tag)
            shutil.copytree(rendered, destination, dirs_exist_ok=True)

    # Always refresh the development manual; replace historical symlinks first.
    current = pages / "manual/current"
    if current.is_symlink():
        current.unlink()
    elif current.exists():
        shutil.rmtree(current)
    with tempfile.TemporaryDirectory(prefix="grooves-current-") as directory:
        output = Path(directory) / "rendered"
        run(str(ROOT / "gradlew"), ":grooves-docs:asciidoctor",
            f"-PmanualVersion={versions['upcoming'][0]}",
            f"-PmanualOutput={output}")
        rendered = output / "html5" if (output / "html5/index.html").is_file() else output
        validate_manual(rendered, "current")
        shutil.copytree(rendered, current)
    add_favicons(pages)

    # The upcoming menu entry must resolve to the freshly built development manual.
    snapshot = pages / "manual" / versions["upcoming"][0]
    if snapshot.is_symlink():
        snapshot.unlink()
    elif snapshot.exists():
        shutil.rmtree(snapshot)
    shutil.copytree(current, snapshot)
    # Vite imports this file when building the landing page.
    (ROOT / "grooves-site/src/versions.json").write_text(json.dumps(versions, indent=2) + "\n")
    run(str(ROOT / "grooves-site/npmw"), "run", "build", cwd=ROOT / "grooves-site")
    shutil.copytree(ROOT / "grooves-site/dist", pages, dirs_exist_ok=True)
    (pages / ".nojekyll").touch()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("pages", type=Path, help="Path to a gh-pages checkout")
    prepare(parser.parse_args().pages.resolve())
