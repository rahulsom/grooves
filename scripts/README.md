The Build workflow publishes the site on pushes to `main`, version tags, and manual
workflow dispatches. It uses the maintained build on `main` to render missing
manuals from each tag's documentation and example sources, so old Gradle wrappers
and dependencies are unnecessary. Stable and prerelease tags are included.

`prepare-site.py` takes an existing `gh-pages` checkout, preserves published
manuals, builds missing ones, refreshes `manual/current`, adds favicon links to
historical HTML, and generates the landing page's version menu from the tags.
The menu shows the latest stable patch for each major/minor pair, with the two
newest minors under `supported`. `upcoming` contains only the next minor's
snapshot (currently `0.11.0-SNAPSHOT`), backed by the development manual.
Publishing is serialized and commits the complete site only after all builds pass.
The first run after merging also backfills existing tags automatically.
It explicitly requests a Pages build after pushing, since pushes made with
`GITHUB_TOKEN` do not trigger one automatically. Pages should continue to use
the root of the `gh-pages` branch as its publishing source.

To prepare a site locally, fetch all tags, install the site's npm dependencies,
and run with Python 3.12 or newer:

```sh
python3 scripts/prepare-site.py /path/to/gh-pages-checkout
```

This prepares files without committing or pushing them. It also updates
`grooves-site/src/versions.json` before building the landing page.

Run helper tests with:

```sh
python3 -m unittest discover -s scripts -p 'test_*.py'
```
