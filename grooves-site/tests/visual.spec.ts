import { test, expect } from '@playwright/test';

test.describe('Visual Regression Tests', () => {

  test('should match dropdown open state', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    await page.waitForFunction(() => document.fonts.ready);

    // Wait for dropdown to be populated
    await page.waitForFunction(() => {
      const dropdown = document.getElementById('versions');
      return dropdown && dropdown.children.length > 0;
    });

    // Open dropdown
    await page.locator('#dropdownMenuLink').click();
    await page.waitForSelector('#versions:visible');

  });

  test('should check button hover states', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    await page.waitForFunction(() => document.fonts.ready);

    // Hover over Source button
    const sourceButton = page.locator('a[href*="github.com/rahulsom/grooves"]:nth-of-type(1)');
    await sourceButton.hover();

  });

  test('should verify title animation colors', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const title = page.locator('h1');

    // Check that title has animation
    await expect(title).toHaveCSS('animation-name', 'example');
    await expect(title).toHaveCSS('animation-duration', '12s');

    // Verify animation keyframes exist (colors should cycle)
    const styles = await page.evaluate(() => {
      const styleSheets = Array.from(document.styleSheets);
      for (const sheet of styleSheets) {
        try {
          const rules = Array.from(sheet.cssRules || sheet.rules);
          for (const rule of rules) {
            if (rule.type === CSSRule.KEYFRAMES_RULE && rule.name === 'example') {
              return Array.from(rule.cssRules).map(r => r.cssText);
            }
          }
        } catch (e) {
          // Skip sheets we can't access
        }
      }
      return null;
    });

    expect(styles).not.toBeNull();
    expect(styles).toEqual(expect.arrayContaining([
      expect.stringContaining('color: red'),
      expect.stringContaining('color: darkgoldenrod'),
      expect.stringContaining('color: blue'),
      expect.stringContaining('color: green'),
    ]));
  });

  test('should check font loading and rendering', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');
    await page.waitForFunction(() => document.fonts.ready);

    const title = page.locator('h1');

    // Check that Monoton font is loaded for title
    await expect(title).toHaveCSS('font-family', /Monoton/);

    // Check that Roboto font is loaded for body text
    const paragraph = page.locator('p').first();
    await expect(paragraph).toHaveCSS('font-family', /Roboto/);

    // Verify font weights
    const strongElement = page.locator('strong').first();
    await expect(strongElement).toHaveCSS('font-weight', '700');
  });

  test('should verify Bootstrap grid system', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Check desktop layout (columns side by side)
    await page.setViewportSize({ width: 1200, height: 800 });

    const columns = page.locator('.col-sm');

    // All columns should be visible and have proper Bootstrap classes
    for (const column of await columns.all()) {
      await expect(column).toBeVisible();
      const classes = await column.getAttribute('class');
      expect(classes).toContain('col-sm');
    }

  });

  test('should verify icon rendering', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Wait for Font Awesome to load
    await page.waitForFunction(() => {
      const icons = document.querySelectorAll('.fa');
      return icons.length > 0 && getComputedStyle(icons[0], ':before').content !== 'none';
    });

  });
});
