import { test, expect } from '@playwright/test';

test.describe('Basic Layout Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should display the main title', async ({ page }) => {
    await expect(page.locator('h1')).toHaveText('Grooves');
    await expect(page.locator('h1')).toHaveClass(/display-3/);
  });

  test('should display the subtitle', async ({ page }) => {
    await expect(page.locator('.lead')).toHaveText('EventSourcing for JVM Languages.');
  });

  test('should have correct page title', async ({ page }) => {
    await expect(page).toHaveTitle('Grooves');
  });

  test('should have all three main buttons', async ({ page }) => {
    // Documentation button (as dropdown toggle)
    const docButton = page.locator('#dropdownMenuLink');
    await expect(docButton).toBeVisible();
    await expect(docButton).toContainText('Documentation');
    
    // Source button
    const sourceButton = page.locator('a[href="http://github.com/rahulsom/grooves"]');
    await expect(sourceButton).toBeVisible();
    await expect(sourceButton).toContainText('Source');
    
    // Issues button
    const issuesButton = page.locator('a[href="http://github.com/rahulsom/grooves/issues"]');
    await expect(issuesButton).toBeVisible();
    await expect(issuesButton).toContainText('Issues');
  });

  test('should have Font Awesome icons in buttons', async ({ page }) => {
    // Check for Font Awesome icons
    await expect(page.locator('#dropdownMenuLink .fa-book')).toBeVisible();
    await expect(page.locator('a[href*="github.com/rahulsom/grooves"] .fa-github')).toBeVisible();
    await expect(page.locator('a[href*="issues"] .fa-bug')).toBeVisible();
  });

  test('should have three feature columns', async ({ page }) => {
    const columns = page.locator('.row .col-sm');
    await expect(columns).toHaveCount(3);
    
    // Check column headings
    await expect(page.locator('h3').filter({ hasText: 'Languages' })).toBeVisible();
    await expect(page.locator('h3').filter({ hasText: 'Reactive Streams' })).toBeVisible();
    await expect(page.locator('h3').filter({ hasText: 'Application Architectures' })).toBeVisible();
  });

  test('should have proper semantic HTML structure', async ({ page }) => {
    // Check for proper container structure
    await expect(page.locator('.container')).toBeVisible();
    await expect(page.locator('#intro')).toBeVisible();
    await expect(page.locator('.row')).toBeVisible();
    
    // Check for proper heading hierarchy
    const h1 = page.locator('h1');
    const h3s = page.locator('h3');
    await expect(h1).toHaveCount(1);
    await expect(h3s).toHaveCount(3);
  });

  test('should load external resources', async ({ page }) => {
    // Wait for fonts and styles to load
    await page.waitForLoadState('networkidle');
    
    // Check that Bootstrap classes are working (Bootstrap default is 1140px for .container)
    const container = page.locator('.container');
    await expect(container).toHaveCSS('max-width', /1140px|none/); // mobile can be 'none'
    
    // Check that buttons have Bootstrap styling
    const buttons = page.locator('.btn');
    for (const button of await buttons.all()) {
      await expect(button).toHaveCSS('padding', /\d+px/);
      await expect(button).toHaveCSS('border-radius', /\d+px/);
    }
  });

  test('should highlight key technologies in bold', async ({ page }) => {
    // Check that key terms are bold in each section
    const strongElements = page.locator('strong');
    
    // Languages section - use more specific selectors to avoid conflicts
    await expect(strongElements.filter({ hasText: 'Java' }).first()).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Groovy' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Kotlin' })).toBeVisible();
    
    // Reactive Streams section
    await expect(strongElements.filter({ hasText: 'Reactive Streams' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'RxJava' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Reactor' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Akka' })).toBeVisible();
    
    // Application Architectures section
    await expect(strongElements.filter({ hasText: 'JavaEE' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Spring' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Grails' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'RDBMS' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Mongo' })).toBeVisible();
    await expect(strongElements.filter({ hasText: 'Guava' })).toBeVisible();
  });
});
