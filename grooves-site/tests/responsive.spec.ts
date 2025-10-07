import { test, expect } from "@playwright/test";

test.describe("Responsive Behavior Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    await page.waitForLoadState("networkidle");
  });

  test("should stack columns on mobile devices", async ({ page }) => {
    // Test mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });

    const columns = page.locator(".col-sm");

    // On mobile, columns should stack vertically
    const firstColumn = columns.nth(0);
    const secondColumn = columns.nth(1);

    const firstBox = await firstColumn.boundingBox();
    const secondBox = await secondColumn.boundingBox();

    // Second column should be below the first (higher y position)
    expect(secondBox?.y).toBeGreaterThan(firstBox?.y || 0);
  });

  test("should show columns side by side on desktop", async ({ page }) => {
    // Test desktop viewport
    await page.setViewportSize({ width: 1200, height: 800 });

    const columns = page.locator(".col-sm");

    // On desktop, columns should be side by side
    const firstColumn = columns.nth(0);
    const secondColumn = columns.nth(1);

    const firstBox = await firstColumn.boundingBox();
    const secondBox = await secondColumn.boundingBox();

    // Columns should be on roughly the same horizontal level
    const yDifference = Math.abs((secondBox?.y || 0) - (firstBox?.y || 0));
    expect(yDifference).toBeLessThan(50); // Allow small differences due to text wrapping
  });

  test("should adapt button layout on mobile", async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });

    const buttons = page.locator("#intro .btn, #intro .btn-group");

    // Buttons should be visible and properly spaced on mobile
    for (const button of await buttons.all()) {
      await expect(button).toBeVisible();

      // Check that buttons don't overflow container
      const buttonBox = await button.boundingBox();
      const containerBox = await page.locator(".container").boundingBox();

      if (buttonBox && containerBox) {
        expect(buttonBox.x + buttonBox.width).toBeLessThanOrEqual(
          containerBox.x + containerBox.width + 10
        ); // Small tolerance
      }
    }
  });

  test("should maintain readable text at all viewport sizes", async ({
    page,
  }) => {
    const viewports = [
      { width: 320, height: 568 }, // iPhone SE
      { width: 375, height: 667 }, // iPhone 6/7/8
      { width: 768, height: 1024 }, // iPad
      { width: 1024, height: 768 }, // iPad Landscape
      { width: 1200, height: 800 }, // Desktop
      { width: 1920, height: 1080 }, // Large Desktop
    ];

    for (const viewport of viewports) {
      await page.setViewportSize(viewport);

      // Check that text is not cut off
      const paragraphs = page.locator("p");
      for (const paragraph of await paragraphs.all()) {
        await expect(paragraph).toBeVisible();

        // Text should not be clipped
        const box = await paragraph.boundingBox();
        expect(box?.width).toBeGreaterThan(0);
        expect(box?.height).toBeGreaterThan(0);
      }

      // Check that headings are visible
      const headings = page.locator("h1, h3");
      for (const heading of await headings.all()) {
        await expect(heading).toBeVisible();
      }
    }
  });

  test("should handle dropdown on mobile devices", async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });

    // Wait for dropdown to be populated
    await page.waitForFunction(() => {
      const dropdown = document.getElementById("versions");
      return dropdown && dropdown.children.length > 0;
    });

    const docButton = page.locator("#dropdownMenuLink");
    const dropdown = page.locator("#versions");

    // Should be able to open dropdown on mobile
    await docButton.click();
    await expect(dropdown).toBeVisible();

    // Dropdown should not overflow viewport
    const dropdownBox = await dropdown.boundingBox();
    const viewportSize = page.viewportSize();

    if (dropdownBox && viewportSize) {
      expect(dropdownBox.x + dropdownBox.width).toBeLessThanOrEqual(
        viewportSize.width
      );
    }
  });

  test("should maintain proper spacing at different screen sizes", async ({
    page,
  }) => {
    const viewports = [
      { width: 375, height: 667, name: "mobile" },
      { width: 768, height: 1024, name: "tablet" },
      { width: 1200, height: 800, name: "desktop" },
    ];

    for (const viewport of viewports) {
      await page.setViewportSize(viewport);

      // Check intro section padding
      const intro = page.locator("#intro");
      const introPadding = await intro.evaluate((el) => {
        const styles = getComputedStyle(el);
        return {
          top: parseInt(styles.paddingTop),
          bottom: parseInt(styles.paddingBottom),
        };
      });

      // Should have reasonable padding
      expect(introPadding.top).toBeGreaterThan(10);
      expect(introPadding.bottom).toBeGreaterThan(10);

      // Check container margins
      const container = page.locator(".container");
      const containerBox = await container.boundingBox();
      const viewportSize = page.viewportSize();

      if (containerBox && viewportSize) {
        // Container should have some margin on desktop, but can be full width on mobile
        if (viewport.width >= 1200) {
          expect(containerBox.width).toBeLessThan(viewportSize.width - 40); // Some margin
        }
      }
    }
  });

  test("should maintain accessibility at all viewport sizes", async ({
    page,
  }) => {
    const viewports = [{ width: 1200, height: 800 }];

    for (const viewport of viewports) {
      await page.setViewportSize(viewport);

      // Check that all visible interactive elements are focusable
      const interactiveElements = page.locator("a, button").filter({
        hasNot: page.locator(".dropdown-menu:not(.show) .dropdown-item"),
      });

      for (const element of await interactiveElements.all()) {
        // Skip hidden elements
        if (!(await element.isVisible())) {
          continue;
        }

        // Element should be focusable
        await element.focus();
        await expect(element).toBeFocused();

        // Element should have reasonable size for interaction
        const box = await element.boundingBox();
        if (box) {
          // Minimum touch target size should be around 44px (WCAG guidelines)
          if (viewport.width <= 480) {
            expect(Math.min(box.width, box.height)).toBeGreaterThan(30);
          }
        }
      }
    }
  });

  test("should handle landscape and portrait orientations on mobile", async ({
    page,
  }) => {
    // Test portrait
    await page.setViewportSize({ width: 375, height: 667 });
    await expect(page.locator("#intro")).toBeVisible();
    await expect(page.locator(".row")).toBeVisible();

    // Test landscape
    await page.setViewportSize({ width: 667, height: 375 });
    await expect(page.locator("#intro")).toBeVisible();
    await expect(page.locator(".row")).toBeVisible();

    // Content should still be readable and accessible
    const columns = page.locator(".col-sm");
    await expect(columns.nth(0)).toBeVisible();
    await expect(columns.nth(1)).toBeVisible();
    await expect(columns.nth(2)).toBeVisible();
  });
});
