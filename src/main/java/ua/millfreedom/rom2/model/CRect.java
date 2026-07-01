package ua.millfreedom.rom2.model;

import java.awt.*;

public class CRect {
    //0x0
    public int left;
    //0x4
    public int top;
    //0x8
    public int right;
    //0xc
    public int bottom;

    /**
     * Native: CRect::New @00402A40.
     * Fully ported. Java default primitive initialization covers the native no-op constructor.
     */
    public CRect() {
        this(0, 0, 0, 0);
    }

    /**
     * Native: CRect::CRect @0041E210.
     * Fully ported.
     */
    public CRect(int l, int t, int r, int b) {
        set(l, t, r, b);
    }

    /**
     * Native: CRect::CRect(const RECT& srcRect).
     * Also covers pointer-style copy construction when source is null.
     * not ported.
     */
    public CRect(CRect srcRect) {
        if (srcRect == null) {
            set(0, 0, 0, 0);
            return;
        }
        set(srcRect);
    }

    /**
     * Native: CRect::CRect @004281B0.
     * Fully ported.
     */
    public CRect(Point point, Dimension size) {
        set(point.x, point.y, point.x + size.width, point.y + size.height);
    }

    /**
     * Native: CRect::CRect(POINT topLeft, POINT bottomRight).
     * not ported.
     */
    public CRect(Point topLeft, Point bottomRight) {
        int l = topLeft == null ? 0 : topLeft.x;
        int t = topLeft == null ? 0 : topLeft.y;
        int r = bottomRight == null ? 0 : bottomRight.x;
        int b = bottomRight == null ? 0 : bottomRight.y;
        set(l, t, r, b);
    }

    /**
     * Native: CRect::Width @0041E250.
     * Fully ported.
     */
    public int width() {
        return right - left;
    }

    /**
     * Native: CRect::Height @00402A70.
     * Fully ported.
     */
    public int height() {
        return bottom - top;
    }

    /**
     * Native: CRect::SetRect @0041E2D0.
     * Fully ported.
     */
    public void set(int l, int t, int r, int b) {
        left = l;
        top = t;
        right = r;
        bottom = b;
    }

    /**
     * Native: CRect::SetEmpty @0041E300.
     * Fully ported.
     */
    public void setEmpty() {
        set(0, 0, 0, 0);
    }

    /**
     * Native: CRect::Copy @00402A50.
     * Fully ported.
     */
    public CRect set(CRect srcRect) {
        set(srcRect.left, srcRect.top, srcRect.right, srcRect.bottom);
        return this;
    }

    /**
     * Native: CRect::IsEmpty @0041E2B0.
     * Fully ported.
     */
    public boolean isEmpty() {
        return right <= left || bottom <= top;
    }

    /**
     * Native: CRect::IsRectNull @00472780.
     * Fully ported.
     */
    public boolean isRectNull() {
        return left == 0 && top == 0 && right == 0 && bottom == 0;
    }

    /**
     * Native: CRect::Intersect @0041E340.
     * Fully ported.
     */
    public void intersect(CRect other) {
        left = Math.max(left, other.left);
        top = Math.max(top, other.top);
        right = Math.min(right, other.right);
        bottom = Math.min(bottom, other.bottom);
        if (isEmpty()) {
            setEmpty();
        }
    }

    /**
     * Native: CRect::Union @0041E360.
     * Fully ported.
     */
    public void unionWith(CRect other) {
        if (other.isEmpty()) {
            return;
        }
        if (isEmpty()) {
            set(other);
            return;
        }
        left = Math.min(left, other.left);
        top = Math.min(top, other.top);
        right = Math.max(right, other.right);
        bottom = Math.max(bottom, other.bottom);
    }

    /**
     * Native: CRect::Offset @0041E320.
     * Fully ported.
     */
    public void offset(int dx, int dy) {
        left += dx;
        right += dx;
        top += dy;
        bottom += dy;
    }

    /**
     * Native: CRect::Subtract @0041E380.
     * Fully ported.
     */
    public boolean subtract(CRect source, CRect remove) {
        set(source);
        if (remove.isEmpty() || isEmpty()
                || remove.right <= left || right <= remove.left
                || remove.bottom <= top || bottom <= remove.top) {
            return !isEmpty();
        }
        if (remove.left <= left && right <= remove.right) {
            if (remove.top <= top) {
                top = Math.min(bottom, remove.bottom);
            } else if (bottom <= remove.bottom) {
                bottom = Math.max(top, remove.top);
            }
        } else if (remove.top <= top && bottom <= remove.bottom) {
            if (remove.left <= left) {
                left = Math.min(right, remove.right);
            } else if (right <= remove.right) {
                right = Math.max(left, remove.left);
            }
        }
        if (isEmpty()) {
            setEmpty();
        }
        return !isEmpty();
    }

    /**
     * Native: CRect::Inflate @0041E4D0.
     * Fully ported.
     */
    public boolean inflate(int dx, int dy) {
        left -= dx;
        right += dx;
        top -= dy;
        bottom += dy;
        return true;
    }

    /**
     * Native: CRect::Intersect @0041E4F0.
     * Fully ported.
     */
    public boolean intersect(CRect first, CRect second) {
        left = Math.max(first.left, second.left);
        top = Math.max(first.top, second.top);
        right = Math.min(first.right, second.right);
        bottom = Math.min(first.bottom, second.bottom);
        if (isEmpty()) {
            setEmpty();
            return false;
        }
        return true;
    }

    /**
     * Native: CRect::NormalizeRect @005B3900.
     * Fully ported.
     */
    public CRect normalize() {
        if (right < left) {
            int tmp = left;
            left = right;
            right = tmp;
        }
        if (bottom < top) {
            int tmp = top;
            top = bottom;
            bottom = tmp;
        }
        return this;
    }

    /**
     * Native: CRect::InflateRect(LPCRECT) @005B391F.
     * Fully ported.
     */
    public CRect inflate(CRect rect) {
        left -= rect.left;
        top -= rect.top;
        right += rect.right;
        bottom += rect.bottom;
        return this;
    }

    /**
     * Native: CRect::InflateRect(int l, int t, int r, int b) @005B393C.
     * Fully ported.
     */
    public CRect inflate(int l, int t, int r, int b) {
        left -= l;
        top -= t;
        right += r;
        bottom += b;
        return this;
    }

    /**
     * Native: CRect::DeflateRect(LPCRECT) @005B395A.
     * Fully ported.
     */
    public CRect deflate(CRect rect) {
        left += rect.left;
        top += rect.top;
        right -= rect.right;
        bottom -= rect.bottom;
        return this;
    }

    /**
     * Native: CRect::DeflateRect(int l, int t, int r, int b) @005B3977.
     * Fully ported.
     */
    public CRect deflate(int l, int t, int r, int b) {
        left += l;
        top += t;
        right -= r;
        bottom -= b;
        return this;
    }

    /**
     * Native: CRect::MulDiv @005B3995.
     * Fully ported.
     */
    public CRect mulDiv(int nMultiplier, int nDivisor) {
        return new CRect(
                mulDivCoordinate(left, nMultiplier, nDivisor),
                mulDivCoordinate(top, nMultiplier, nDivisor),
                mulDivCoordinate(right, nMultiplier, nDivisor),
                mulDivCoordinate(bottom, nMultiplier, nDivisor)
        );
    }

    /**
     * Native support extracted from CRect::MulDiv @005B3995 for Win32 ::MulDiv coordinate semantics.
     */
    private static int mulDivCoordinate(int value, int nMultiplier, int nDivisor) {
        if (nDivisor == 0) {
            return -1;
        }
        long product = (long) value * nMultiplier;
        long denominator = nDivisor;
        long absProduct = Math.abs(product);
        long absDenominator = Math.abs(denominator);
        long rounded = (absProduct + absDenominator / 2) / absDenominator;
        long signed = ((product < 0) ^ (denominator < 0)) ? -rounded : rounded;
        if (signed < Integer.MIN_VALUE || signed > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) signed;
    }

    //return new normalized CRect from this not ported.
    public CRect normalized() {
        return new CRect(this).normalize();
    }

    /**
     * Native: CRect::InRect @0041E3D0.
     * Fully ported.
     */
    public boolean contains(int x, int y) {
        return x >= left && x < right && y >= top && y < bottom;
    }

    /**
     * Native: CRect::InRect @0041E3D0.
     * Fully ported.
     */
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    /**
     * Compares rectangle coordinates.
     * not ported.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CRect other)) {
            return false;
        }
        return left == other.left
                && top == other.top
                && right == other.right
                && bottom == other.bottom;
    }

    /**
     * Hashes rectangle coordinates consistently with equals.
     * not ported.
     */
    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }

    // not ported.
    public String toString() {
        return "(" + this.left + "," + this.top + "," + this.right + "," + this.bottom + ")";
    }
}
