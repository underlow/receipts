# Design System

## Overview
This document outlines the design system for the Receipt Dashboard application, which follows the shadcn/ui design principles. The system provides a consistent, modern, and accessible user interface across all components.

## Design Philosophy

### Core Principles
- **Consistency**: All components follow the same design patterns and color scheme
- **Accessibility**: Proper contrast ratios and semantic HTML structure
- **Responsiveness**: Components adapt seamlessly to different screen sizes
- **Modern Aesthetics**: Clean, minimal design with subtle shadows and borders
- **User-Centered**: Focus on usability and clear information hierarchy

### Visual Language
- **Minimalist**: Clean layouts with purposeful whitespace
- **Subtle Depth**: Light shadows and borders instead of heavy effects
- **Neutral Colors**: Professional color palette with accent colors for actions
- **Typography**: Modern font stack with appropriate weights and sizes

## Color System

### CSS Custom Properties
The design system uses HSL color values defined as CSS custom properties for consistent theming:

```css
:root {
    --background: 0 0% 100%;           /* Pure white */
    --foreground: 222.2 84% 4.9%;      /* Near black */
    --card: 0 0% 100%;                 /* White cards */
    --card-foreground: 222.2 84% 4.9%; /* Dark text on cards */
    --popover: 0 0% 100%;              /* White popover backgrounds */
    --popover-foreground: 222.2 84% 4.9%; /* Dark text on popovers */
    --primary: 222.2 47.4% 11.2%;      /* Dark blue for primary actions */
    --primary-foreground: 210 40% 98%; /* Light text on primary */
    --secondary: 210 40% 96%;          /* Light gray for secondary elements */
    --secondary-foreground: 222.2 47.4% 11.2%; /* Dark text on secondary */
    --muted: 210 40% 96%;              /* Muted backgrounds */
    --muted-foreground: 215.4 16.3% 46.9%; /* Muted text */
    --accent: 210 40% 96%;             /* Accent backgrounds */
    --accent-foreground: 222.2 47.4% 11.2%; /* Dark text on accent */
    --destructive: 0 84.2% 60.2%;      /* Red for destructive actions */
    --destructive-foreground: 210 40% 98%; /* Light text on destructive */
    --border: 214.3 31.8% 91.4%;       /* Light gray borders */
    --input: 214.3 31.8% 91.4%;        /* Input field borders */
    --ring: 222.2 84% 4.9%;            /* Focus ring color */
    --radius: 0.5rem;                  /* Standard border radius */
}
```

### Color Usage
- **Primary**: Main action buttons, active navigation states
- **Secondary**: Less prominent buttons, secondary actions
- **Muted**: Backgrounds, disabled states, placeholder text
- **Destructive**: Delete buttons, error states
- **Border**: Separators, component outlines

### Status Colors
- **Success/Done**: `hsl(142.1 76.2% 36.3%)` - Green for completed states
- **Warning/In Progress**: `hsl(47.9 95.8% 53.1%)` - Yellow for pending states
- **Error/Failed**: `hsl(var(--destructive))` - Red for error states

## Typography

### Font Stack
```css
font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
```

### Font Sizes
- **Large Headings**: 1.875rem (30px)
- **Medium Headings**: 1.25rem (20px)
- **Body Text**: 0.875rem (14px)
- **Small Text**: 0.75rem (12px)

### Font Weights
- **Light**: 300 (rarely used)
- **Regular**: 400 (body text)
- **Medium**: 500 (emphasis, buttons)
- **Semi-bold**: 600 (headings, important text)

## Layout System

### Spacing Scale
All spacing follows a consistent scale using rem units:
- **Extra Small**: 0.25rem (4px)
- **Small**: 0.5rem (8px)
- **Medium**: 0.75rem (12px)
- **Large**: 1rem (16px)
- **Extra Large**: 1.5rem (24px)
- **XXL**: 2rem (32px)

### Grid System
- **Container**: Full-width fluid container
- **Content Area**: 1.5rem padding on all sides
- **Component Spacing**: 1.5rem margin between major components

## Component Design System

### Cards
**Class**: `.dashboard-card`
- Background: `hsl(var(--card))`
- Border: `1px solid hsl(var(--border))`
- Border radius: `var(--radius)`
- Padding: `1.5rem`
- Hover effect: Subtle shadow lift

### Buttons

#### Primary Button
**Class**: `.action-btn-primary`
- Background: `hsl(var(--primary))`
- Color: `hsl(var(--primary-foreground))`
- Border radius: `calc(var(--radius) - 2px)`
- Padding: `0.375rem 0.75rem`

#### Secondary Button
**Class**: `.action-btn`
- Background: `hsl(var(--background))`
- Color: `hsl(var(--foreground))`
- Border: `1px solid hsl(var(--border))`
- Hover: `hsl(var(--muted))`

#### Destructive Button
**Class**: `.action-btn-danger`
- Background: `hsl(var(--destructive))`
- Color: `hsl(var(--destructive-foreground))`
- Hover: Opacity reduction

### Navigation

#### Header Navigation
**Class**: `.dashboard-header`
- Background: `hsl(var(--card))`
- Border bottom: `1px solid hsl(var(--border))`
- Padding: `0.75rem 1.5rem`

#### Navigation Links
**Class**: `.navbar-nav .nav-link`
- Color: `hsl(var(--muted-foreground))`
- Hover: `hsl(var(--muted))` background
- Active: `hsl(var(--primary))` background

#### Sidebar Navigation
**Class**: `.navigation-panel`
- Background: `hsl(var(--card))`
- Border: `1px solid hsl(var(--border))`
- Padding: `1rem`

## Table Design System

### Table Structure
**Class**: `.modern-table-wrapper`
- Background: `hsl(var(--card))`
- Border: `1px solid hsl(var(--border))`
- Border radius: `var(--radius)`
- Overflow: Hidden

### Table Header
**Class**: `.modern-table-header`
- Background: `hsl(var(--muted))`
- Border bottom: `1px solid hsl(var(--border))`
- Font weight: 500
- Text transform: Uppercase
- Letter spacing: 0.025em

### Table Rows
**Class**: `.modern-table-row`
- Border bottom: `1px solid hsl(var(--border))`
- Hover: `hsl(var(--muted))` background
- Transition: `background-color 0.2s ease`

### Table Cells
**Class**: `.modern-table-cell`
- Padding: `0.875rem 1rem`
- Vertical align: Middle
- Font size: `0.875rem`

### Status Indicators
**Class**: `.status-indicator`
- Display: Inline flex
- Padding: `0.125rem 0.5rem`
- Border radius: `calc(var(--radius) - 2px)`
- Font size: `0.75rem`
- Font weight: 500

#### Status Types
- **Done**: Green background with white text
- **In Progress**: Yellow background with dark text
- **Pending**: Secondary background with muted text

### Form Elements

#### Input Fields
**Class**: `.form-control`
- Border: `1px solid hsl(var(--border))`
- Border radius: `var(--radius)`
- Padding: `0.5rem 0.75rem`
- Focus: Ring color with shadow

#### Checkboxes
**Class**: `.form-check-input`
- Size: `1rem × 1rem`
- Border: `2px solid hsl(var(--border))`
- Border radius: `calc(var(--radius) / 2)`
- Checked: `hsl(var(--primary))` background

### Dropdown Menus
**Class**: `.dropdown-menu`
- Background: `hsl(var(--popover))`
- Border: `1px solid hsl(var(--border))`
- Border radius: `var(--radius)`
- Shadow: `0 4px 12px -4px rgba(0, 0, 0, 0.1)`
- Padding: `0.25rem`

### Empty States
**Class**: `.empty-state-cell`
- Padding: `5rem 1.25rem`
- Background: `hsl(var(--muted))`
- Text align: Center

**Content Structure**:
- Icon: `3rem` font size, muted color
- Title: `1.25rem` font size, semibold
- Description: `0.875rem` font size, muted color

### Pagination
**Class**: `.pagination`
- Gap: `0.5rem`
- Margin top: `1.25rem`

**Page Links**:
- Border: `1px solid hsl(var(--border))`
- Border radius: `calc(var(--radius) - 2px)`
- Padding: `0.5rem 0.75rem`
- Active: Primary background

## Responsive Design

### Breakpoints
- **Mobile**: ≤ 576px
- **Tablet**: 577px - 768px
- **Desktop**: ≥ 769px

### Mobile Adaptations
- Reduced padding: `0.75rem`
- Smaller font sizes
- Stacked navigation
- Horizontal scrolling for tables
- Collapsed action buttons

### Tablet Adaptations
- Medium padding: `1rem`
- Flexible navigation layout
- Maintained table structure

## Accessibility

### Color Contrast
- All text meets WCAG 2.1 AA standards
- Minimum 4.5:1 ratio for normal text
- Minimum 3:1 ratio for large text

### Keyboard Navigation
- All interactive elements focusable
- Visible focus indicators
- Logical tab order

### Semantic HTML
- Proper heading hierarchy
- Descriptive link text
- Form labels and ARIA attributes

### Screen Reader Support
- ARIA labels for complex components
- Status announcements
- Table headers properly associated

## Implementation Guidelines

### CSS Architecture
- Use CSS custom properties for colors
- Maintain component-based structure
- Follow BEM naming convention where appropriate
- Use relative units (rem, em) for scalability

### Component Development
- Start with base styles
- Add component-specific modifications
- Ensure responsive behavior
- Include hover and focus states

### Color Usage
- Always use CSS custom properties
- Never hard-code color values
- Test in both light and dark contexts
- Consider colorblind accessibility

### Typography
- Use the established font stack
- Maintain consistent line heights
- Apply appropriate font weights
- Ensure proper heading hierarchy

## File Structure

### Templates
- `src/main/resources/templates/dashboard.html` - Main dashboard with embedded styles
- `src/main/resources/templates/fragments/table.html` - Reusable table components

### Styles
- Embedded CSS in dashboard.html using CSS custom properties
- Component-specific styles organized by functionality
- Responsive styles using media queries

## Future Enhancements

### Planned Features
- Dark mode support using CSS custom properties
- Additional component variants
- Animation and transition improvements
- Enhanced accessibility features

### Maintenance
- Regular accessibility audits
- Performance optimization
- Browser compatibility testing
- Design system documentation updates

This design system ensures consistency, accessibility, and maintainability across the entire Receipt Dashboard application while following modern web design principles.
