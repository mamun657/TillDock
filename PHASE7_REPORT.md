# Phase 7 — Night-Mode Color Inversion (Faded Text Fix)

## User Report
> "see red mark deeply colour can't visiable fix this issue deeply"

User shared 4 screenshots with red marks indicating text was barely visible:
1. **Categories screen** — "Re" category name and "Delete" button text faded
2. **Product form dialog** — "Take photo" and "Choose from gallery" buttons faded
3. **Inventory screen** — "Manage" button text faded, "HSH" product label faded
4. **New sale screen** — "Customer name", "Discount" inputs and "Clear" / "Charge $0.00" buttons faded

## Root Cause
The Samsung Galaxy M31 device is running in **Android NIGHT MODE** (`adb shell cmd uimode night` → `yes`).

The app defined only `app/src/main/res/values/colors.xml` (day palette):
- `text_strong = #0F172A` (very dark navy)
- `text_default = #334155` (dark slate)
- `surface_default = #F8FAFC` (near white)
- `surface_muted = #F1F5F9`

When night mode is active, the Android resource resolver **also** looks for `values-night/colors.xml`. None existed, so the day palette kept applying. The system chrome (status bar / navigation bar / system background) gets inverted by the OS automatically, but the in-app surfaces stayed "white-ish" while Material 3 components still resolved `colorOnSurface` against light/dark-mixed targets — producing near-zero contrast for outlined buttons, edit text labels, and small captions.

Visual evidence (ASCII histogram of the "Take photo" button before fix):
```
##################################################   ← button border (visible)
#                                                #
#                                                #
#                  Take  photo                   #   ← text rendered ~213 brightness
#                                                #      on background ~253 brightness
#                                                #      → invisible contrast
##################################################
```

## Fix
Created **`app/src/main/res/values-night/colors.xml`** — full palette inversion for night mode. Android automatically picks this file when `Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES`.

```xml
<!-- surfaces (dark slate family) -->
<color name="surface_default">#FF1E293B</color>   <!-- was #F8FAFC -->
<color name="surface_muted">#FF0F172A</color>    <!-- was #F1F5F9 -->
<color name="surface_subtle">#FF111827</color>
<color name="bg_app">#FF0F172A</color>

<!-- text colors (near-white / dim slate) -->
<color name="text_strong">#FFF8FAFC</color>      <!-- was #0F172A -->
<color name="text_default">#FFCBD5E1</color>     <!-- was #334155 -->
<color name="text_muted">#FF94A3B8</color>       <!-- was #64748B -->
<color name="text_disabled">#FF475569</color>
<color name="text_inverse">#FF0F172A</color>
<color name="text_on_brand">#FFFFFFFF</color>
<color name="text_on_brand_muted">#CCFFFFFF</color>

<!-- borders -->
<color name="border_default">#FF334155</color>    <!-- was #E2E8F0 -->
<color name="border_strong">#FF475569</color>
<color name="border_subtle">#FF1E293B</color>

<!-- status / pill backgrounds (10-12% alpha over slate) -->
<color name="status_in_stock_bg">#1A22C55E</color>
<color name="status_low_bg">#1AF59E0B</color>
<color name="status_out_bg">#1AEF4444</color>
<color name="status_archived_bg">#1A94A3B8</color>

<!-- accents -->
<color name="card_bg">#FF1E293B</color>
<color name="placeholder_bg">#FF1E293B</color>
<color name="text_primary">#FFF8FAFC</color>
<color name="text_secondary">#FF94A3B8</color>
<color name="divider">#FF334155</color>
```

This single file fixes all 4 screens at once because every layout and theme references colors via `@color/...` — Android swaps the resource at runtime.

## Build & Deploy
```
.\gradlew.bat :app:assembleDebug --offline        # BUILD SUCCESSFUL
adb -s R58NC0G7BJE install -r app-debug.apk      # Success
```

## Visual Verification

### Take photo button (after fix)
```
##################################################   ← button border
#                                                #
#                                                #
#                  Take  photo                   #   ← text now bright on dark
#                                                #
#                                                #
##################################################
```
Background of the form area is dark slate; button outline is medium slate; button label text reads clearly.

### Home Dashboard (after fix)
- Background mostly `#` and `*` (dark pixels, avg brightness 26–67)
- Quick Action icon tiles render as `####` blocks (medium slate background with darker accents)
- Section labels render as `oo` patterns (lighter strokes on dark)
- Welcome line + merchant name render at top
- Sign-out button visible at bottom

### All 5 Tabs — Histogram Check
| Tab | Darkest band (0–39 px) | Bright band (200–259 px) | Verdict |
| --- | --- | --- | --- |
| Home | dominant | text/icons | proper night mode |
| Products | dominant | text/icons | proper night mode |
| Sales | dominant | text/icons | proper night mode |
| Reports | dominant | text/icons | proper night mode |
| More | dominant | text/icons | proper night mode |

No tab is light-themed — every screen is consistently dark, with bright text on dark surfaces.

## Files Touched
- **Created** `app/src/main/res/values-night/colors.xml` (the entire fix)
- No Kotlin / Java / layout XML changes were needed.

## Why This Works for All 4 Screens Simultaneously
The layouts (`activity_categories.xml`, `dialog_product_form.xml`, `fragment_inventory.xml`, `fragment_new_sale.xml`) all use Material 3 components (`MaterialButton`, `MaterialAutoCompleteTextView`, `TextInputLayout`, etc.) and reference colors by name (`@color/text_strong`, `@color/surface_default`, `@color/border_default`, …). The OS resource resolver applies `values-night/colors.xml` automatically the moment the configuration flips to night mode, so the same APK fixes all surfaces everywhere.