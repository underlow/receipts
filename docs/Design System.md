# Design System for Refero

This design system document provides implementation-ready specifications for building the Refero UI with Bootstrap and custom CSS. It covers color palette, typography, spacing, elevation (shadows), and common components.

---

## 1. Color Palette

| Token Name      | Hex                      | Usage                                   | Bootstrap Variable   |
|-----------------|--------------------------|-----------------------------------------|----------------------|
| Primary         | `#4A3AFF`                | Buttons, links, accents                 | `$primary`           |
| Secondary       | `#F4F2FF`                | Background highlights                   | `$secondary`         |
| Surface         | `#FFFFFF`                | Card backgrounds, panels                | `$body-bg`           |
| Surface Light   | `#F9F9FB`                | Hover states, input backgrounds         | —                    |
| Border          | `#E3E1E8`                | Dividers, borders                       | `$gray-200`          |
| Text Primary    | `#13131A`                | Main body text                          | `$gray-900`          |
| Text Secondary  | `#52525B`                | Subdued labels, metadata                | `$gray-600`          |
| Text Tertiary   | `#8F8F9B`                | Disabled text                           | `$gray-400`          |
| Interactive BG  | `#F2F0FF`                | Interactive input focus/hover           | —                    |
| Shadow Color    | `rgba(18, 15, 68, 0.04)` | Card shadows                            | —                    |

---

## 2. Typography

- **Font Family:** `Inter, sans-serif`  
- **Base Font Size:** 16px (`1rem`)  
- **Line Height:** 1.5

| Style Name    | Bootstrap Class | Font Size | Line Height | Weight | Letter Spacing |
|---------------|-----------------|-----------|-------------|--------|----------------|
| Display Title | `.h1`           | 28px      | 36px        | 600    | normal         |
| Heading       | `.h3`           | 20px      | 28px        | 600    | normal         |
| Body Large    | `.lead`         | 16px      | 24px        | 400    | normal         |
| Body Regular  | `.body-text`¹   | 14px      | 20px        | 400    | normal         |
| Label         | `.small`        | 12px      | 16px        | 500    | normal         |
| Mono Code     | `.code`         | 14px      | 20px        | 400    | normal         |

¹ Define utility:
\`\`\`css
.body-text {
  font-size: 0.875rem;
  line-height: 1.4286;
}
\`\`\`

---

## 3. Spacing

Refer to the Bootstrap spacing scale and supplement with custom variables where needed.

| Name                | Bootstrap Class   | Pixels |
|---------------------|-------------------|--------|
| Spacing XS          | `.p-1` / `.m-1`   | 4px    |
| Spacing SM          | `.p-2` / `.m-2`   | 8px    |
| Spacing MD          | `.p-3` / `.m-3`   | 16px   |
| Spacing LG          | `.p-4` / `.m-4`   | 24px   |
| Spacing XL          | `.p-5` / `.m-5`   | 32px   |
| Horizontal Gutter   | `.gx-4`           | 24px   |
| Vertical Gutter     | `.gy-3`           | 16px   |

Custom CSS variables:
\`\`\`css
:root {
  --spacing-xxs: 2px;
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
}
\`\`\`

---

## 4. Elevation & Shadows

| Level             | CSS Class      | Shadow                                        |
|-------------------|----------------|-----------------------------------------------|
| Card (Default)    | `.shadow-sm`   | `0 1px 2px rgba(18, 15, 68, 0.04)`            |
| Modal / Overlay   | `.shadow`      | `0 4px 8px rgba(18, 15, 68, 0.08)`            |
| Top App Bar       | `.shadow-none` | none                                          |

Custom shadow mixin:
\`\`\`scss
@mixin card-shadow {
  box-shadow: 0 1px 2px rgba(18, 15, 68, 0.04);
}
\`\`\`

---

## 5. Component Specifications

### 5.1 Navbar (Top Bar)

- **Structure:** `.navbar`, `.navbar-expand`, `.bg-white`, `.shadow-none`
- **Height:** 64px  
- **Padding:** `0 var(--spacing-md)` (0 16px)  
- **Logo:** 24px height, aligned left  
- **Navigation Items:** `.nav-link text-secondary`, 14px, bold on hover  
- **Search Input:** `.form-control`, width: 240px, height: 36px

### 5.2 Sidebar (Vertical Nav)

- **Width:** 240px  
- **Background:** `$surface (#FFFFFF)`  
- **Items:** flex-col, gap `--spacing-md`  
- **Icon + Label:** icon size 20px, label `.small text-secondary`  
- **Active Item:** `.text-primary font-semibold`

### 5.3 Cards & Panels

**Base Card:**
\`\`\`html
<div class="card shadow-sm border-0">
  <div class="card-body p-4">
    <!-- Content -->
  </div>
</div>
\`\`\`
- **Border Radius:** 8px  
- **Padding:** `--spacing-md` (16px)  
- **Background:** `$surface`  
- **Shadow:** card-shadow mixin

**Org Chart Panel:**
- **Title:** `.h5` (16px, 24px, weight 600)  
- **Import Button:** `.btn btn-outline-primary btn-sm`  
- **List Items:** `.list-group` with `.list-group-item d-flex align-items-center`  
- **Avatar:** 32×32px, border-radius: 50%  
- **Role Text:** `.body-text ms-2`

### 5.4 Jobs Panel

- **Structure:** same as Org Chart Panel  
- **Job Item:** `.list-group-item` with title `.body-text font-medium` and subtitle `.small text-secondary`  
- **Button:** `.btn btn-outline-secondary btn-sm`

### 5.5 Share Panel (Embed & Links)

- **Tab Nav:** `.nav nav-tabs border-0`  
- **Input Group:** `.input-group`  
- **Copy Button:** `.btn btn-primary btn-sm`  
- **Tab Content Padding:** `--spacing-md`

---

## 6. Utility Classes

\`\`\`css
.text-primary       { color: #4A3AFF !important; }
.text-secondary     { color: #52525B !important; }
.bg-surface         { background-color: #FFFFFF !important; }
.bg-surface-light   { background-color: #F9F9FB !important; }
.border-default     { border-color: #E3E1E8 !important; }
.rounded-md         { border-radius: 8px !important; }
.shadow-card        { box-shadow: 0 1px 2px rgba(18, 15, 68, 0.04) !important; }
\`\`\`

---

## 7. Accessibility & States

- **Focus State:** `outline: 2px solid #4A3AFF` on inputs and buttons  
- **Hover State:** lighten backgrounds by 8% (`#F2F0FF`) on interactive elements  
- **Disabled State:** `.opacity-50 cursor-not-allowed`

---

*Document version: 1.0. Last updated: 2025-07-08.*  
