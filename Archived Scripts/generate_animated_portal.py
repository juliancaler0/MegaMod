"""Generate animated museum door portal texture - 16x128 PNG (8 frames of 16x16)."""
import struct, zlib, os, math

OUT = "src/main/resources/assets/megamod/textures/block/museum_door_portal.png"

def write_png(path, w, h, pixels):
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
    print(f"Written: {path}")

def clamp(v, lo=0, hi=255):
    return max(lo, min(hi, int(v)))

def lerp_c(c1, c2, t):
    return tuple(clamp(c1[i] + (c2[i] - c1[i]) * t) for i in range(len(c1)))

NUM_FRAMES = 8
W, H = 16, 16

# Color palette
DEEP_PURPLE = (40, 8, 60)
MID_PURPLE = (74, 14, 107)
BRIGHT_PURPLE = (123, 47, 175)
LIGHT_PURPLE = (160, 80, 210)
HIGHLIGHT = (204, 119, 255)
SPARKLE = (238, 200, 255)
WHITE = (255, 240, 255)

def gen_frame(frame_idx):
    """Generate one 16x16 frame of the portal animation."""
    pixels = [[(0,0,0,0) for _ in range(W)] for _ in range(H)]
    t = frame_idx / NUM_FRAMES  # 0.0 to ~0.875
    angle = t * 2 * math.pi

    for y in range(H):
        for x in range(W):
            cx, cy = x - 7.5, y - 7.5
            dist = math.sqrt(cx*cx + cy*cy)

            # Base purple with radial gradient
            base_t = min(1.0, dist / 10.0)
            r = DEEP_PURPLE[0] + (MID_PURPLE[0] - DEEP_PURPLE[0]) * (1 - base_t)
            g = DEEP_PURPLE[1] + (MID_PURPLE[1] - DEEP_PURPLE[1]) * (1 - base_t)
            b = DEEP_PURPLE[2] + (MID_PURPLE[2] - DEEP_PURPLE[2]) * (1 - base_t)

            # Swirling pattern - rotating bright bands
            theta = math.atan2(cy, cx)
            swirl = math.sin(theta * 3 + angle * 2 + dist * 0.5) * 0.5 + 0.5
            r += (BRIGHT_PURPLE[0] - r) * swirl * 0.4
            g += (BRIGHT_PURPLE[1] - g) * swirl * 0.4
            b += (BRIGHT_PURPLE[2] - b) * swirl * 0.4

            # Pulsing concentric rings
            ring = math.sin(dist * 1.5 - angle * 3) * 0.5 + 0.5
            r += (LIGHT_PURPLE[0] - r) * ring * 0.25
            g += (LIGHT_PURPLE[1] - g) * ring * 0.25
            b += (LIGHT_PURPLE[2] - b) * ring * 0.25

            # Drifting highlight spot
            hx = 7.5 + math.cos(angle) * 4
            hy = 7.5 + math.sin(angle * 0.7) * 4
            hdist = math.sqrt((x - hx)**2 + (y - hy)**2)
            if hdist < 3.5:
                ht = 1.0 - hdist / 3.5
                ht = ht * ht  # quadratic falloff
                r += (HIGHLIGHT[0] - r) * ht * 0.6
                g += (HIGHLIGHT[1] - g) * ht * 0.6
                b += (HIGHLIGHT[2] - b) * ht * 0.6

            # Second drifting highlight (opposite side)
            hx2 = 7.5 + math.cos(angle + math.pi) * 3
            hy2 = 7.5 + math.sin(angle * 0.7 + math.pi) * 3
            hdist2 = math.sqrt((x - hx2)**2 + (y - hy2)**2)
            if hdist2 < 2.5:
                ht2 = 1.0 - hdist2 / 2.5
                ht2 = ht2 * ht2
                r += (SPARKLE[0] - r) * ht2 * 0.5
                g += (SPARKLE[1] - g) * ht2 * 0.5
                b += (SPARKLE[2] - b) * ht2 * 0.5

            # Sparkle pixels (deterministic based on position + frame)
            spark_hash = ((x * 73 + y * 137 + frame_idx * 29) % 47)
            if spark_hash < 2:
                sparkle_intensity = 0.4 + (spark_hash / 2.0) * 0.4
                r += (WHITE[0] - r) * sparkle_intensity
                g += (WHITE[1] - g) * sparkle_intensity
                b += (WHITE[2] - b) * sparkle_intensity

            # Center glow pulse
            if dist < 2:
                pulse = math.sin(angle * 2) * 0.3 + 0.5
                center_t = (1.0 - dist / 2.0) * pulse
                r += (WHITE[0] - r) * center_t * 0.3
                g += (WHITE[1] - g) * center_t * 0.3
                b += (WHITE[2] - b) * center_t * 0.3

            pixels[y][x] = (clamp(r), clamp(g), clamp(b), 255)

    return pixels


if __name__ == "__main__":
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    # Build full strip: 16 wide x (16 * 8) tall
    total_h = H * NUM_FRAMES
    all_pixels = [[(0,0,0,0) for _ in range(W)] for _ in range(total_h)]

    print(f"Generating {NUM_FRAMES} frames of portal animation...")
    for f in range(NUM_FRAMES):
        frame = gen_frame(f)
        for y in range(H):
            for x in range(W):
                all_pixels[f * H + y][x] = frame[y][x]

    write_png(OUT, W, total_h, all_pixels)
    print("Done! Museum door portal animation generated.")
