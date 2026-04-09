"""Generate phone OFF and ON textures for MegaMod - 16x16 RGBA PNGs."""
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

# Shared phone hardware pixels (right side of texture: cols 10-15)
def draw_phone_hardware(img):
    body_dark = (26, 26, 30, 255)
    body = (34, 34, 40, 255)
    body_light = (42, 42, 48, 255)
    cam_body = (28, 28, 34, 255)
    cam_lens = (16, 16, 21, 255)
    cam_ring = (64, 64, 80, 255)
    flash_yellow = (255, 215, 0, 255)
    speaker_dark = (8, 8, 16, 255)
    front_cam = (20, 20, 30, 255)
    port_dark = (10, 10, 18, 255)
    button = (50, 50, 58, 255)

    # Camera island (cols 10-12, rows 0-2)
    fill_rect(img, 10, 0, 13, 3, cam_body)
    set_px(img, 10, 0, cam_ring)
    set_px(img, 12, 0, cam_ring)
    set_px(img, 10, 2, cam_ring)
    set_px(img, 12, 2, cam_ring)

    # Camera lens (col 10, row 4)
    set_px(img, 10, 4, cam_lens)

    # Flash LED (col 11, row 4)
    set_px(img, 11, 4, flash_yellow)

    # Speaker grille (cols 10-11, row 6)
    set_px(img, 10, 6, speaker_dark)
    set_px(img, 11, 6, (20, 20, 28, 255))

    # Front camera (col 10, row 7)
    set_px(img, 10, 7, front_cam)

    # Charging port (cols 10-11, row 8)
    set_px(img, 10, 8, port_dark)
    set_px(img, 11, 8, port_dark)

    # Body sides (cols 12-13, full height)
    for y in range(16):
        t = ((y * 3 + 5) % 7) / 10.0
        set_px(img, 12, y, lerp(body, body_dark, t))
        set_px(img, 13, y, lerp(body_dark, body, t))

    # Buttons (col 13, rows 4-6)
    set_px(img, 13, 4, button)
    set_px(img, 13, 5, (45, 45, 52, 255))
    set_px(img, 13, 6, button)

    # Body front/back (cols 14-15)
    for y in range(16):
        t = y / 15.0
        set_px(img, 14, y, lerp(body, body_dark, t * 0.3))
        set_px(img, 15, y, lerp(body_dark, body, t * 0.3))


def gen_phone_off():
    """Phone with dark/off screen."""
    img = make_blank()

    screen_black = (10, 10, 14, 255)
    screen_reflect = (18, 18, 24, 255)
    screen_highlight = (25, 25, 32, 255)
    bezel = (26, 26, 30, 255)

    # Screen area (cols 0-9, rows 0-15) - dark with subtle reflection
    for y in range(16):
        for x in range(10):
            # Base dark screen
            c = screen_black
            # Diagonal reflection streak (top-left to center)
            diag = x + y
            if 4 <= diag <= 7:
                t = 1.0 - abs(diag - 5.5) / 2.0
                c = lerp(screen_black, screen_highlight, t * 0.6)
            elif 8 <= diag <= 10:
                t = 1.0 - abs(diag - 9.0) / 2.0
                c = lerp(screen_black, screen_reflect, t * 0.4)
            set_px(img, x, y, c)

    # Top bezel row
    for x in range(10):
        set_px(img, x, 0, bezel)
    # Bottom bezel row
    for x in range(10):
        set_px(img, x, 15, bezel)

    draw_phone_hardware(img)
    return img


def gen_phone_on():
    """Phone with lit screen showing wallpaper and app icons."""
    img = make_blank()

    bezel = (26, 26, 30, 255)

    # Wallpaper gradient (deep blue at top -> purple at bottom)
    wall_top = (20, 40, 90, 255)
    wall_mid = (40, 30, 100, 255)
    wall_bot = (60, 25, 80, 255)

    # Screen background - gradient wallpaper
    for y in range(16):
        for x in range(10):
            t = y / 15.0
            if t < 0.5:
                c = lerp(wall_top, wall_mid, t * 2)
            else:
                c = lerp(wall_mid, wall_bot, (t - 0.5) * 2)
            # Subtle radial glow in center
            dx = abs(x - 5)
            dy = abs(y - 7)
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < 5:
                glow_t = 1.0 - dist / 5.0
                c = lerp(c, (80, 60, 140, 255), glow_t * 0.2)
            set_px(img, x, y, c)

    # Status bar (row 0) - bezel/dark with signal + battery
    for x in range(10):
        set_px(img, x, 0, bezel)
    # Signal bars (cols 1-3)
    set_px(img, 1, 0, (180, 180, 180, 255))
    set_px(img, 2, 0, (200, 200, 200, 255))
    set_px(img, 3, 0, (220, 220, 220, 255))
    # Battery (cols 7-8)
    set_px(img, 7, 0, (80, 220, 80, 255))
    set_px(img, 8, 0, (80, 220, 80, 255))

    # Clock area (row 2, centered) - "12:00" represented as bright pixels
    set_px(img, 3, 2, (220, 220, 240, 255))
    set_px(img, 4, 2, (220, 220, 240, 255))
    set_px(img, 5, 2, (180, 180, 200, 255))
    set_px(img, 6, 2, (220, 220, 240, 255))
    set_px(img, 7, 2, (220, 220, 240, 255))

    # Date line (row 3)
    for x in range(2, 8):
        set_px(img, x, 3, (140, 140, 170, 255))

    # App icon grid (rows 5-12, 2 columns of 2x2 icons with gaps)
    # App colors - vibrant modern colors
    apps = [
        # (x, y, color) - top-left corner of 2x2 icon
        (1, 5, (60, 180, 80, 255)),    # Green (messages)
        (4, 5, (50, 140, 240, 255)),   # Blue (browser)
        (7, 5, (240, 80, 60, 255)),    # Red (phone)
        (1, 8, (255, 180, 0, 255)),    # Orange (settings)
        (4, 8, (180, 60, 220, 255)),   # Purple (music)
        (7, 8, (0, 200, 200, 255)),    # Cyan (camera)
        (1, 11, (220, 220, 60, 255)),  # Yellow (notes)
        (4, 11, (220, 100, 160, 255)), # Pink (store)
        (7, 11, (100, 100, 240, 255)), # Indigo (map)
    ]

    for ax, ay, color in apps:
        dark = tuple(max(0, c - 40) for c in color[:3]) + (255,)
        light = tuple(min(255, c + 30) for c in color[:3]) + (255,)
        # 2x2 icon with bevel
        set_px(img, ax, ay, light)
        set_px(img, ax + 1, ay, color)
        set_px(img, ax, ay + 1, color)
        set_px(img, ax + 1, ay + 1, dark)

    # Home bar (row 14) - thin white line
    for x in range(3, 7):
        set_px(img, x, 14, (180, 180, 200, 255))

    # Bottom bezel
    for x in range(10):
        set_px(img, x, 15, bezel)

    draw_phone_hardware(img)
    return img


if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)
    print("Generating phone textures...")
    write_png(os.path.join(OUT_DIR, "phone.png"), gen_phone_off())
    write_png(os.path.join(OUT_DIR, "phone_on.png"), gen_phone_on())
    print("Done!")
