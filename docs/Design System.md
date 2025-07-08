# Design System

## Overview
This document outlines the design system for the Receipt Dashboard application, including table styling, component patterns, and visual guidelines.

## Table Design System

### Modern Table Architecture
The application uses a modern table design system that provides a clean, professional appearance with enhanced usability.

#### Visual Characteristics
- **Clean Layout**: White background with subtle shadows and rounded corners
- **Consistent Spacing**: 16px-20px padding for comfortable reading
- **Subtle Borders**: Light gray separators between rows
- **Hover Effects**: Smooth transitions on interactive elements
- **Responsive Design**: Adapts to different screen sizes

### Table Components

#### 1. Table Wrapper
**Class**: `.modern-table-wrapper`
- Background: White
- Border radius: 8px
- Box shadow: `0 2px 4px rgba(0, 0, 0, 0.05)`
- Overflow: Hidden for clean corners

#### 2. Table Structure
**Class**: `.modern-table`
- Width: 100%
- Border collapse: Collapsed
- Font size: 0.875rem (14px)

#### 3. Table Header
**Class**: `.modern-table-header`
- Background: Light gray (`#f8f9fa`)
- Border bottom: 2px solid `#e9ecef`
- Font weight: 600
- Color: `#495057`

**Header Cells**:
- Padding: 16px-20px
- Text align: Left
- No border
- White space: nowrap

**Sortable Headers**:
- Cursor: pointer
- User select: none
- Hover background: `#e9ecef`
- Transition: background-color 0.2s ease

#### 4. Table Body
**Class**: `.modern-table-body`
- Background: White

**Table Rows**:
- **Class**: `.modern-table-row`
- Border bottom: 1px solid `#f1f3f4`
- Hover background: `#f8f9fa`
- Transition: background-color 0.2s ease

**Table Cells**:
- **Class**: `.modern-table-cell`
- Padding: 16px-20px
- Vertical align: middle
- Color: `#212529`
- Font size: 0.875rem

### Status Indicators

#### Design Pattern
Status indicators use a unified visual system with colored backgrounds and icons.

#### Status Types

##### Done Status
**Class**: `.status-done`
- Background: `#d4edda` (light green)
- Color: `#155724` (dark green)
- Icon: Green dot (`�`)
- Usage: Completed items, processed documents

##### In Progress Status
**Class**: `.status-in-progress`
- Background: `#fff3cd` (light yellow)
- Color: `#856404` (dark yellow)
- Icon: Yellow star (``)
- Usage: Pending items, processing documents

#### Implementation
```html
<span class="status-indicator status-done">Done</span>
<span class="status-indicator status-in-progress">In Process</span>
```

### Action Buttons

#### Design Pattern
Action buttons follow a consistent design with proper spacing and hover effects.

#### Button Types

##### Primary Action Button
**Class**: `.action-btn-primary`
- Background: `#007bff` (blue)
- Color: White
- Border: `#007bff`
- Hover: `#0056b3`

##### Danger Action Button
**Class**: `.action-btn-danger`
- Background: `#dc3545` (red)
- Color: White
- Border: `#dc3545`
- Hover: `#c82333`

##### Default Action Button
**Class**: `.action-btn`
- Background: White
- Color: `#495057`
- Border: `#dee2e6`
- Hover: `#f8f9fa`

#### Button Container
**Class**: `.action-buttons`
- Display: flex
- Gap: 8px
- Align items: center

#### Dropdown Toggle
**Class**: `.dropdown-toggle-btn`
- Background: none
- Border: none
- Color: `#6c757d`
- Hover: `#f8f9fa`
- Icon: `�` (vertical dots)

### Empty State

#### Design Pattern
Empty states provide clear communication when no data is available.

#### Structure
**Class**: `.empty-state-cell`
- Padding: 60px 20px
- Background: `#fafbfc`
- Text align: center

**Content Container**:
- **Class**: `.empty-state-content`
- Display: flex column
- Align items: center
- Gap: 12px

**Icon**: `.empty-state-icon`
- Font size: 2.5rem
- Color: `#adb5bd`

**Title**: `.empty-state-title`
- Font size: 1.125rem
- Font weight: 600
- Color: `#495057`

**Text**: `.empty-state-text`
- Color: `#6c757d`

### Pagination

#### Design Pattern
Pagination follows a clean, modern design with proper spacing and hover effects.

#### Styling
- Margin top: 20px
- Page links: 8px-12px padding
- Border radius: 4px
- Font size: 0.875rem

#### States
- **Default**: `#495057` color, `#dee2e6` border
- **Active**: `#007bff` background, white text
- **Hover**: `#f8f9fa` background

### Responsive Design

#### Mobile Breakpoints
**768px and below**:
- Horizontal scrolling enabled
- Reduced padding (12px-16px)
- Smaller font sizes (0.8125rem)
- Stacked action buttons

#### Implementation
```css
@media (max-width: 768px) {
    .modern-table-wrapper {
        overflow-x: auto;
    }
    
    .modern-table-header th,
    .modern-table-cell {
        padding: 12px 16px;
        font-size: 0.8125rem;
    }
    
    .action-buttons {
        flex-direction: column;
        gap: 4px;
    }
}
```

### Color System

#### Primary Colors
- **Primary Blue**: `#007bff`
- **Success Green**: `#28a745`
- **Warning Yellow**: `#ffc107`
- **Danger Red**: `#dc3545`

#### Neutral Colors
- **Dark Gray**: `#495057`
- **Medium Gray**: `#6c757d`
- **Light Gray**: `#adb5bd`
- **Border Gray**: `#dee2e6`
- **Background Gray**: `#f8f9fa`

#### Status Colors
- **Success Background**: `#d4edda`
- **Success Text**: `#155724`
- **Warning Background**: `#fff3cd`
- **Warning Text**: `#856404`

### Typography

#### Font Stack
- Primary: 'Roboto', sans-serif
- Weight: 300, 400, 500, 600, 700

#### Font Sizes
- **Large**: 1.125rem (18px)
- **Normal**: 0.875rem (14px)
- **Small**: 0.8125rem (13px)

### Implementation Files

#### Templates
- `/src/main/resources/templates/fragments/table.html` - Main table template
- `/src/main/resources/templates/dashboard.html` - Dashboard with embedded styles

#### Kotlin Components
- `/src/main/kotlin/me/underlow/receipt/dashboard/BaseTable.kt` - Base table functionality
- `/src/main/kotlin/me/underlow/receipt/dashboard/BillsView.kt` - Bills table implementation
- `/src/main/kotlin/me/underlow/receipt/dashboard/InboxView.kt` - Inbox table implementation

### Usage Guidelines

#### When to Use
- All data tables in the application
- Bills and inbox listing pages
- Any tabular data presentation

#### Customization
- Status indicators can be extended with new types
- Action buttons can be configured per table
- Column sorting can be enabled/disabled per column
- Search functionality is optional per table

#### Best Practices
- Always provide empty state messaging
- Use consistent status indicators across tables
- Implement hover states for interactive elements
- Ensure responsive behavior on mobile devices
- Maintain consistent spacing and typography

### Future Enhancements

#### Planned Features
- Drag handle indicators for reorderable rows
- Bulk selection checkboxes
- Column customization options
- Export functionality integration
- Advanced filtering controls

#### Accessibility
- ARIA labels for screen readers
- Keyboard navigation support
- High contrast mode compatibility
- Focus indicators for interactive elements
