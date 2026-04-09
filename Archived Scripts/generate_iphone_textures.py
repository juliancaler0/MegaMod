"""Generate iPhone-style phone OFF and ON textures for MegaMod - 16x16 RGBA PNGs.

Texture layout (16x16):
  Cols 0-9:   Front face (bezel frame + screen content)
    Col 0:      Right bezel edge
    Col 9:      Left bezel edge
    Row 0:      Top bezel
    Row 15:     Bottom bezel
    Cols 1-8, Rows 1-14: Screen content area

  Cols 10-15: Back & hardware
    (10-12, 0-2):  Camera module background
    (10, 3):       Camera lens color
    (11, 3):       Flash LED color
    (10-11, 4):    Dynamic island color
    (10, 5-7):     Button color
    (11, 5):       Mute switch accent
    Col 12-13:     Body side edges (gradient)
    Col 14-15:     Back plate (gradient)
"""
import struct, zlib, os

OUT_DIR = "src/main/resources/assets/megamod/textures/item"

def write_png(path, pixels):
    w, h = 16, 16
    raw = b''
    for y in range(h):
        raw += b'\x00'
        for x in range(w):
            r, g, b, a = pixels[y][x]
            raw += struct.pack('BBBB', r, g, b, a)
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    idat = zlib.compress(raw)
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        f.write(chunk(b'IHDR', ihdr))
        f.write(chunk(b'IDAT', idat))
        f.write(chunk(b'IEND', b''))
    print(f"  Written: {path}")

def lerp(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(len(c1)))

def make_blank():
    return [[(0,0,0,0) for _ in range(16)] for _ in range(16)]

def set_px(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img[y][x] = c

def fill_rect(img, x1, y1, x2, y2, c):
    for yy in range(y1, y2):
        for xx in range(x1, x2):
            set_px(img, xx, yy, c)

# ── Color palette ──
BEZEL       = (18, 18, 22, 255)
BODY        = (46, 46, 52, 255)
BODY_DARK   = (36, 36, 42, 255)
BODY_LIGHT  = (56, 56, 62, 255)
CAM_BODY    = (30, 30, 36, 255)
CAM_LENS    = (8,  8, 14, 255)
CAM_RING    = (65, 65, 80, 255)
FLASH       = (255, 220, 60, 255)
DYN_ISLAND  = (5,  5,  8, 255)
BUTTON      = (58, 58, 66, 255)
MUTE_ACCENT = (220, 120, 40, 255)


def draw_hardware(img):
    """Draw the right half of texture: camera, body, buttons (shared by both states)."""

    # Camera module background (cols 10-12, rows 0-2)
    fill_rect(img, 10, 0, 13, 3, CAM_BODY)
    # Camera ring highlights at corners
    set_px(img, 10, 0, CAM_RING)
    set_px(img, 12, 0, CAM_RING)
    set_px(img, 10, 2, CAM_RING)
    set_px(img, 12, 2, CAM_RING)
    # Lens circles inside module (subtle)
    set_px(img, 10, 0, lerp(CAM_LENS, CAM_RING, 0.3))
    set_px(img, 12, 0, lerp(CAM_LENS, CAM_RING, 0.3))
    set_px(img, 10, 2, lerp(CAM_LENS, CAM_RING, 0.3))
    # Flash position in module
    set_px(img, 12, 1, FLASH)

    # Individual lens pixel (col 10, row 3) — used by lens_1/2/3 elements
    set_px(img, 10, 3, CAM_LENS)
    # Flash pixel (col 11, row 3) — reference
    set_px(img, 11, 3, FLASH)

    # Dynamic island color (cols 10-11, row 4)
    set_px(img, 10, 4, DYN_ISLAND)
    set_px(img, 11, 4, DYN_ISLAND)

    # Button color (col 10, rows 5-7) — power/volume
    for r in range(5, 8):
        set_px(img, 10, r, BUTTON)
    # Mute switch accent (col 11, row 5)
    set_px(img, 11, 5, MUTE_ACCENT)

    # Body side strips (cols 12-13, full height) — subtle gradient
    for y in range(16):
        t = y / 15.0
        c1 = lerp(BODY_LIGHT, BODY_DARK, t * 0.4 + 0.3)
        c2 = lerp(BODY, BODY_DARK, t * 0.3 + 0.2)
        set_px(img, 12, y, c1)
        set_px(img, 13, y, c2)

    # Back plate (cols 14-15, full height) — slight top-down gradient
    for y in range(16):
        t = y / 15.0
        c1 = lerp(BODY_LIGHT, BODY, t * 0.4)
        c2 = lerp(BODY, BODY_DARK, t * 0.35)
        set_px(img, 14, y, c1)
        set_px(img, 15, y, c2)


def draw_bezel_frame(img):
    """Draw the dark bezel border around the screen area."""
    # Top bezel (row 0, cols 0-9)
    for x in range(10):
        set_px(img, x, 0, BEZEL)
    # Bottom bezel (row 15, cols 0-9)
    for x in range(10):
        set_px(img, x, 15, BEZEL)
    # Left bezel (col 9, full height)
    for y in range(16):
        set_px(img, 9, y, BEZEL)
    # Right bezel (col 0, full height)
    for y in range(16):
        set_px(img, 0, y, BEZEL)


def gen_phone_off():
    """iPhone with dark off-screen and subtle reflection glare."""
    img = make_blank()

    screen_black     = (8,  8, 12, 255)
    screen_reflect   = (22, 22, 30, 255)
    screen_highlight = (32, 32, 44, 255)

    draw_bezel_frame(img)

    # Screen area (cols 1-8, rows 1-14): dark with diagonal reflection
    for y in range(1, 15):
        for x in range(1, 9):
            c = screen_black
            # Primary diagonal glare (top-left to center)
            diag = x + y
            if 3 <= diag <= 7:
                t = 1.0 - abs(diag - 5.0) / 2.5
                c = lerp(screen_black, screen_highlight, max(0, t) * 0.65)
            elif 8 <= diag <= 11:
                t = 1.0 - abs(diag - 9.5) / 2.0
                c = lerp(screen_black, screen_reflect, max(0, t) * 0.35)
            # Faint secondary reflection (bottom-right area)
            diag2 = x + y
            if 14 <= diag2 <= 17:
                t2 = 1.0 - abs(diag2 - 15.5) / 2.0
                c = lerp(c, screen_reflect, max(0, t2) * 0.2)
            set_px(img, x, y, c)

    draw_hardware(img)
    return img


def gen_phone_on():
    """iPhone with lit home screen: status bar, clock, app grid, dock."""
    img = make_blank()

    # Wallpaper gradient (deep blue → indigo → purple, iPhone-style)
    wall_top = (20, 45, 115, 255)
    wall_mid = (45, 30, 105, 255)
    wall_bot = (65, 25, 85, 255)

    draw_bezel_frame(img)

    # Fill screen with wallpaper gradient + subtle center glow
    for y in range(1, 15):
        for x in range(1, 9):
            t = (y - 1) / 13.0
            if t < 0.5:
                c = lerp(wall_top, wall_mid, t * 2)
            else:
                c = lerp(wall_mid, wall_bot, (t - 0.5) * 2)
            # Subtle radial glow
            dx = abs(x - 4.5)
            dy = abs(y - 7.0)
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < 5:
                glow = 1.0 - dist / 5.0
                c = lerp(c, (85, 65, 155, 255), glow * 0.15)
            set_px(img, x, y, c)

    # ── Status bar (row 1) ──
    set_px(img, 1, 1, (190, 190, 205, 255))  # signal
    set_px(img, 2, 1, (200, 200, 215, 255))
    set_px(img, 3, 1, (210, 210, 225, 255))
    set_px(img, 7, 1, (75, 225, 75, 255))    # battery green
    set_px(img, 8, 1, (75, 225, 75, 255))

    # ── Dynamic Island hint (row 2, centered) ──
    set_px(img, 4, 2, (12, 12, 18, 255))
    set_px(img, 5, 2, (12, 12, 18, 255))

    # ── Clock (row 3) ──
    clk = (225, 225, 242, 255)
    set_px(img, 2, 3, clk)
    set_px(img, 3, 3, clk)
    set_px(img, 4, 3, (165, 165, 185, 255))  # colon
    set_px(img, 5, 3, clk)
    set_px(img, 6, 3, clk)

    # ── Date (row 4) ──
    for x in range(2, 7):
        set_px(img, x, 4, (145, 145, 172, 255))

    # ── App icon grid — 4 per row, each 2x2 ──
    def draw_icon_row(apps, start_row):
        for i, color in enumerate(apps):
            bx = 1 + i * 2
            light = tuple(min(255, c + 35) for c in color[:3]) + (255,)
            dark  = tuple(max(0,   c - 45) for c in color[:3]) + (255,)
            set_px(img, bx,     start_row,     light)
            set_px(img, bx + 1, start_row,     color)
            set_px(img, bx,     start_row + 1, color)
            set_px(img, bx + 1, start_row + 1, dark)

    row1 = [(55,190,70,255), (50,140,245,255), (245,75,55,255), (255,175,0,255)]
    row2 = [(175,55,225,255), (0,195,205,255), (255,60,100,255), (90,90,245,255)]
    row3 = [(255,210,0,255), (100,210,120,255), (230,125,60,255), (160,160,175,255)]

    draw_icon_row(row1, 5)   # rows 5-6
    draw_icon_row(row2, 7)   # rows 7-8
    draw_icon_row(row3, 9)   # rows 9-10

    # ── Dock background (rows 11-12): frosted glass effect ──
    for y in [11, 12]:
        for x in range(1, 9):
            t = (y - 1) / 13.0
            wc = lerp(wall_mid, wall_bot, (t - 0.5) * 2) if t >= 0.5 else lerp(wall_top, wall_mid, t * 2)
            frost = lerp(wc, (175, 175, 195, 255), 0.3)
            set_px(img, x, y, frost)

    dock = [(55,190,70,255), (50,140,245,255), (245,165,0,255), (30,120,255,255)]
    draw_icon_row(dock, 11)  # rows 11-12

    # ── Home bar (row 14, centered white line) ──
    for x in range(3, 7):
        set_px(img, x, 14, (195, 195, 212, 255))

    # Row 13: just wallpaper (already filled)

    draw_hardware(img)
    return img


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Generating iPhone-style phone textures...")
    write_png(os.path.join(OUT_DIR, "phone.png"), gen_phone_off())
    write_png(os.path.join(OUT_DIR, "phone_on.png"), gen_phone_on())
    print("Done!")
