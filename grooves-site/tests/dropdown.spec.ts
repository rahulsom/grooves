import { test, expect } from "@playwright/test";

test.describe("Dropdown Functionality Tests", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto("/");
    // Wait for JavaScript to load and populate dropdown
    await page.waitForFunction(() => {
      const dropdown = document.getElementById("versions");
      return dropdown && dropdown.children.length > 0;
    });
  });

  test("should show dropdown when clicking Documentation button", async ({
    page,
  }) => {
    const dropdown = page.locator("#versions");
    const docButton = page.locator("#dropdownMenuLink");

    // Initially dropdown should not be visible
    await expect(dropdown).not.toBeVisible();

    // Click documentation button
    await docButton.click();

    // Dropdown should become visible
    await expect(dropdown).toBeVisible();
  });

  test("should populate dropdown with version data from versions.json", async ({
    page,
  }) => {
    const docButton = page.locator("#dropdownMenuLink");
    await docButton.click();

    const dropdown = page.locator("#versions");
    await expect(dropdown).toBeVisible();

    // Check for supported versions (should be bold for recommended)
    await expect(dropdown.locator('a[href*="0.7.0"]')).toBeVisible();
    await expect(dropdown.locator('a[href*="0.6.1"]')).toBeVisible();

    // Check for upcoming versions section
    await expect(
      dropdown.locator(".dropdown-header").filter({ hasText: "Upcoming" })
    ).toBeVisible();
    await expect(dropdown.locator('a[href*="0.8.0-SNAPSHOT"]')).toBeVisible();

    // Check for old versions section
    await expect(
      dropdown.locator(".dropdown-header").filter({ hasText: "Old" })
    ).toBeVisible();
    await expect(dropdown.locator('a[href*="0.5.0"]')).toBeVisible();
    await expect(dropdown.locator('a[href*="0.4.0"]')).toBeVisible();
    await expect(dropdown.locator('a[href*="0.3.0"]')).toBeVisible();
    await expect(dropdown.locator('a[href*="0.2.1"]')).toBeVisible();
    await expect(dropdown.locator('a[href*="0.1.1"]')).toBeVisible();
  });

  test("should have proper dropdown structure with headers and dividers", async ({
    page,
  }) => {
    const docButton = page.locator("#dropdownMenuLink");
    await docButton.click();

    const dropdown = page.locator("#versions");

    // Check for dropdown headers
    const headers = dropdown.locator(".dropdown-header");
    await expect(headers).toHaveCount(2); // "Upcoming" and "Old"

    // Check for dropdown dividers
    const dividers = dropdown.locator(".dropdown-divider");
    await expect(dividers).toHaveCount(2); // Before "Upcoming" and before "Old"
  });

  test("should mark recommended version as bold", async ({ page }) => {
    const docButton = page.locator("#dropdownMenuLink");
    await docButton.click();

    const dropdown = page.locator("#versions");

    // First supported version should be bold (recommended)
    const recommendedLink = dropdown.locator("a").first();
    await expect(recommendedLink.locator("strong")).toBeVisible();
  });

  test("should have correct href patterns for documentation links", async ({
    page,
  }) => {
    const docButton = page.locator("#dropdownMenuLink");
    await docButton.click();

    const dropdown = page.locator("#versions");
    const links = dropdown.locator('a[href*="manual/"]');

    // Should have at least 7 version links (2 supported + 1 upcoming + 5 old)
    await expect(links).toHaveCount(8);

    // Check href pattern
    for (const link of await links.all()) {
      const href = await link.getAttribute("href");
      expect(href).toMatch(/^manual\/.*\/index\.html$/);
    }
  });

  test("should close dropdown when clicking outside", async ({ page }) => {
    const docButton = page.locator("#dropdownMenuLink");
    const dropdown = page.locator("#versions");

    // Open dropdown
    await docButton.click();
    await expect(dropdown).toBeVisible();

    // Click outside dropdown
    await page.locator("h1").click();

    // Dropdown should close
    await expect(dropdown).not.toBeVisible();
  });

  test("should handle keyboard navigation", async ({ page }) => {
    const docButton = page.locator("#dropdownMenuLink");

    // Focus the button and press Enter to open
    await docButton.focus();
    await page.keyboard.press("Enter");

    const dropdown = page.locator("#versions");
    await expect(dropdown).toBeVisible();

    // Press Escape to close
    await page.keyboard.press("Escape");
    await expect(dropdown).not.toBeVisible();
  });

  test("should load versions data successfully", async ({ page }) => {
    await page.reload();

    // Wait for the versions to be loaded
    await page.waitForFunction(() => {
      const dropdown = document.getElementById("versions");
      return dropdown && dropdown.children.length > 0;
    });

    // Open the dropdown to make items visible
    const dropdownButton = page.locator("#dropdownMenuLink");
    await dropdownButton.click();
    await page.waitForTimeout(100); // Small delay for dropdown animation

    // Verify that versions data is properly loaded and rendered
    const versionItems = page.locator("#versions .dropdown-item");
    await expect(versionItems.first()).toBeVisible();

    // Check that we have version sections (supported, upcoming, old)
    const headers = page.locator("#versions .dropdown-header");
    await expect(headers).toHaveCount(2); // "Upcoming" and "Old" headers
  });
});
