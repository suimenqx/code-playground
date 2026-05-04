package com.example.slicerush;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    private GameView gameView;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override protected void onPause() {
        super.onPause();
        if (gameView != null) gameView.pauseGameLoop();
    }

    @Override protected void onResume() {
        super.onResume();
        if (gameView != null) gameView.resumeGameLoop();
    }

    static class GameView extends View {
        static final int READY = 0, RUNNING = 1, GAME_OVER = 2;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rnd = new Random();
        final ArrayList<Fruit> fruits = new ArrayList<>();
        final ArrayList<Piece> pieces = new ArrayList<>();
        final ArrayList<Particle> particles = new ArrayList<>();
        final ArrayList<TrailPoint> trail = new ArrayList<>();
        ToneGenerator tones;
        long lastNs = 0L;
        int state = READY, score = 0, best = 0, lives = 3, combo = 0, level = 1;
        float spawnTimer = 0f, comboTimer = 0f, shake = 0f, messageTimer = 0f;
        String message = "";
        boolean loopRunning = true;

        GameView(android.content.Context c) {
            super(c);
            setFocusable(true);
            setKeepScreenOn(true);
            p.setStrokeCap(Paint.Cap.ROUND);
            text.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            try { tones = new ToneGenerator(AudioManager.STREAM_MUSIC, 70); } catch (Throwable ignored) { tones = null; }
        }

        void pauseGameLoop() { loopRunning = false; }
        void resumeGameLoop() { loopRunning = true; lastNs = 0L; postInvalidateOnAnimation(); }

        @Override protected void onDraw(Canvas c) {
            long now = System.nanoTime();
            float dt = lastNs == 0L ? 0.016f : Math.min(0.034f, (now - lastNs) / 1_000_000_000f);
            lastNs = now;
            update(dt);
            drawGame(c);
            if (loopRunning) postInvalidateOnAnimation();
        }

        void startGame() {
            state = RUNNING;
            score = 0; lives = 3; combo = 0; level = 1;
            spawnTimer = 0.35f; comboTimer = 0f; shake = 0f; messageTimer = 1.2f; message = "开始！";
            fruits.clear(); pieces.clear(); particles.clear(); trail.clear();
            play(ToneGenerator.TONE_PROP_ACK, 90);
        }

        void gameOver() {
            state = GAME_OVER;
            best = Math.max(best, score);
            shake = 0.6f;
            messageTimer = 0f;
            play(ToneGenerator.TONE_SUP_ERROR, 450);
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }

        void update(float dt) {
            fadeTrail(dt);
            if (messageTimer > 0) messageTimer -= dt;
            if (shake > 0) shake -= dt;
            if (comboTimer > 0) comboTimer -= dt; else combo = 0;

            if (state == RUNNING) {
                level = 1 + score / 18;
                spawnTimer -= dt;
                if (spawnTimer <= 0) {
                    spawnWave();
                    spawnTimer = Math.max(0.42f, 1.18f - level * 0.055f - rnd.nextFloat() * 0.18f);
                }

                for (int i = fruits.size() - 1; i >= 0; i--) {
                    Fruit f = fruits.get(i);
                    f.update(dt);
                    if (f.y > getHeight() + f.r * 2f) {
                        fruits.remove(i);
                        if (!f.bomb) {
                            lives--;
                            combo = 0; comboTimer = 0f;
                            burst(f.x, getHeight() - 30f, 0x99ffffff, 8, false);
                            play(ToneGenerator.TONE_PROP_NACK, 120);
                            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                            message = "漏掉水果！"; messageTimer = 0.65f;
                            if (lives <= 0) gameOver();
                        }
                    }
                }
            }

            for (int i = pieces.size() - 1; i >= 0; i--) {
                Piece s = pieces.get(i);
                s.update(dt);
                if (s.life <= 0 || s.y > getHeight() + 180) pieces.remove(i);
            }
            for (int i = particles.size() - 1; i >= 0; i--) {
                Particle part = particles.get(i);
                part.update(dt);
                if (part.life <= 0) particles.remove(i);
            }
        }

        void spawnWave() {
            int count = 1 + rnd.nextInt(Math.min(4, 1 + level / 2));
            for (int i = 0; i < count; i++) spawnFruit(i, count);
        }

        void spawnFruit(int index, int count) {
            float w = Math.max(1, getWidth()), h = Math.max(1, getHeight());
            Fruit f = new Fruit();
            f.type = rnd.nextInt(5);
            f.bomb = score >= 4 && rnd.nextFloat() < Math.min(0.18f, 0.08f + level * 0.012f);
            f.r = f.bomb ? 44f + rnd.nextFloat() * 8f : 48f + rnd.nextFloat() * 18f;
            float lane = (index + 1f) / (count + 1f);
            f.x = 70f + lane * (w - 140f) + (-45f + rnd.nextFloat() * 90f);
            f.y = h + f.r + rnd.nextFloat() * 100f;
            f.vx = -260f + rnd.nextFloat() * 520f;
            f.vy = -(980f + rnd.nextFloat() * 420f + Math.min(320f, level * 28f));
            f.gravity = 940f + rnd.nextFloat() * 140f;
            f.rot = rnd.nextFloat() * 6.28f;
            f.spin = -5f + rnd.nextFloat() * 10f;
            fruits.add(f);
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            int action = e.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                if (state == READY || state == GAME_OVER) startGame();
                addTouchPoint(e.getX(), e.getY(), false);
                return true;
            }
            if (state != RUNNING) return true;
            if (action == MotionEvent.ACTION_MOVE) {
                for (int i = 0; i < e.getHistorySize(); i++) addTouchPoint(e.getHistoricalX(i), e.getHistoricalY(i), true);
                addTouchPoint(e.getX(), e.getY(), true);
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                trail.clear();
                return true;
            }
            return true;
        }

        void addTouchPoint(float x, float y, boolean cut) {
            if (!trail.isEmpty() && cut) {
                TrailPoint last = trail.get(trail.size() - 1);
                sliceSegment(last.x, last.y, x, y);
            }
            trail.add(new TrailPoint(x, y));
            while (trail.size() > 18) trail.remove(0);
        }

        void sliceSegment(float x1, float y1, float x2, float y2) {
            if (dist(x1, y1, x2, y2) < 3f) return;
            int hit = 0;
            for (int i = fruits.size() - 1; i >= 0; i--) {
                Fruit f = fruits.get(i);
                if (distanceToSegment(f.x, f.y, x1, y1, x2, y2) <= f.r + 20f) {
                    fruits.remove(i);
                    if (f.bomb) {
                        burst(f.x, f.y, 0xffff5a5f, 38, true);
                        shake = 0.85f;
                        gameOver();
                        return;
                    } else {
                        hit++;
                        combo++;
                        comboTimer = 1.05f;
                        int gain = 1 + Math.max(0, combo - 1) / 2;
                        score += gain;
                        spawnPieces(f);
                        burst(f.x, f.y, f.color(), 20, false);
                        play(ToneGenerator.TONE_PROP_BEEP, 45);
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                }
            }
            if (hit > 1) {
                score += hit;
                message = "COMBO x" + hit;
                messageTimer = 0.75f;
            }
        }

        void spawnPieces(Fruit f) {
            for (int side = -1; side <= 1; side += 2) {
                Piece s = new Piece();
                s.x = f.x; s.y = f.y; s.r = f.r; s.type = f.type; s.side = side; s.color = f.color();
                s.vx = f.vx * 0.45f + side * (160f + rnd.nextFloat() * 80f);
                s.vy = f.vy * 0.12f - 120f - rnd.nextFloat() * 90f;
                s.rot = f.rot; s.spin = side * (4f + rnd.nextFloat() * 5f);
                pieces.add(s);
            }
        }

        void burst(float x, float y, int color, int count, boolean hot) {
            for (int i = 0; i < count; i++) {
                float a = rnd.nextFloat() * 6.28318f;
                float speed = (hot ? 260f : 120f) + rnd.nextFloat() * (hot ? 360f : 210f);
                Particle part = new Particle();
                part.x = x; part.y = y; part.vx = (float)Math.cos(a) * speed; part.vy = (float)Math.sin(a) * speed;
                part.r = hot ? 5f + rnd.nextFloat() * 10f : 4f + rnd.nextFloat() * 8f;
                part.life = hot ? 0.7f + rnd.nextFloat() * 0.45f : 0.45f + rnd.nextFloat() * 0.35f;
                part.color = color;
                particles.add(part);
            }
        }

        void fadeTrail(float dt) {
            for (int i = trail.size() - 1; i >= 0; i--) {
                TrailPoint t = trail.get(i);
                t.life -= dt * 2.6f;
                if (t.life <= 0) trail.remove(i);
            }
        }

        void drawGame(Canvas c) {
            c.save();
            if (shake > 0) {
                float m = 12f * shake;
                c.translate(-m + rnd.nextFloat() * 2f * m, -m + rnd.nextFloat() * 2f * m);
            }
            drawBackground(c);
            for (Piece s : pieces) s.draw(c, p);
            for (Fruit f : fruits) f.draw(c, p);
            for (Particle part : particles) part.draw(c, p);
            drawTrail(c);
            c.restore();
            drawHud(c);
        }

        void drawBackground(Canvas c) {
            int w = getWidth(), h = getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, 0, h, 0xff111827, 0xff16213f, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);
            p.setColor(0x18ffffff);
            for (int i = 0; i < 12; i++) {
                float x = (i * 173 + 51) % Math.max(1, w);
                float y = (i * 257 + 99) % Math.max(1, h);
                c.drawCircle(x, y, 2 + (i % 4), p);
            }
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            p.setColor(0x18ffffff);
            for (int i = 0; i < 6; i++) c.drawCircle(w * 0.5f, h * 0.15f, 90 + i * 72, p);
            p.setStyle(Paint.Style.FILL);
        }

        void drawTrail(Canvas c) {
            if (trail.size() < 2) return;
            Path path = new Path();
            TrailPoint first = trail.get(0);
            path.moveTo(first.x, first.y);
            for (int i = 1; i < trail.size(); i++) path.lineTo(trail.get(i).x, trail.get(i).y);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(20f);
            p.setColor(0x55ffffff);
            c.drawPath(path, p);
            p.setStrokeWidth(8f);
            p.setColor(0xeeffffff);
            c.drawPath(path, p);
            p.setStyle(Paint.Style.FILL);
        }

        void drawHud(Canvas c) {
            int w = getWidth(), h = getHeight();
            text.setShader(null);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(46f);
            text.setColor(0xffffffff);
            c.drawText("Score " + score, 28, 62, text);
            text.setTextSize(28f);
            text.setColor(0xffcbd5e1);
            c.drawText("Best " + best + "   Lv." + level, 30, 101, text);
            text.setTextAlign(Paint.Align.RIGHT);
            text.setTextSize(42f);
            text.setColor(0xffff6b6b);
            c.drawText(hearts(), w - 28, 64, text);
            if (combo > 1 && comboTimer > 0) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(34f + Math.min(18f, combo * 2f));
                text.setColor(0xffffdd55);
                c.drawText("连击 x" + combo, w / 2f, 112, text);
            }
            if (messageTimer > 0 && message.length() > 0) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(42f);
                text.setColor(0xffffffff);
                c.drawText(message, w / 2f, h * 0.24f, text);
            }
            if (state == READY) drawCenterPanel(c, "SLICE RUSH", "滑动切开水果，避开炸弹", "点击或滑动开始");
            else if (state == GAME_OVER) drawCenterPanel(c, "游戏结束", "得分 " + score + "    最高 " + best, "点击重新开始");
        }

        String hearts() {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < lives; i++) b.append('♥');
            return b.toString();
        }

        void drawCenterPanel(Canvas c, String title, String subtitle, String hint) {
            float w = getWidth(), h = getHeight();
            RectF card = new RectF(w * 0.08f, h * 0.35f, w * 0.92f, h * 0.62f);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xbb000000);
            c.drawRoundRect(card, 34, 34, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(0x55ffffff);
            c.drawRoundRect(card, 34, 34, p);
            p.setStyle(Paint.Style.FILL);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(0xffffffff);
            text.setTextSize(60f);
            c.drawText(title, w / 2f, card.top + 78f, text);
            text.setTextSize(31f);
            text.setColor(0xffdbeafe);
            c.drawText(subtitle, w / 2f, card.top + 132f, text);
            text.setTextSize(30f);
            text.setColor(0xffffdd55);
            c.drawText(hint, w / 2f, card.top + 188f, text);
        }

        void play(int tone, int ms) {
            try { if (tones != null) tones.startTone(tone, ms); } catch (Throwable ignored) {}
        }

        float dist(float x1, float y1, float x2, float y2) {
            return (float)Math.hypot(x2 - x1, y2 - y1);
        }

        float distanceToSegment(float px, float py, float x1, float y1, float x2, float y2) {
            float dx = x2 - x1, dy = y2 - y1;
            float len = dx * dx + dy * dy;
            if (len <= 0.001f) return dist(px, py, x1, y1);
            float t = Math.max(0f, Math.min(1f, ((px - x1) * dx + (py - y1) * dy) / len));
            return dist(px, py, x1 + t * dx, y1 + t * dy);
        }
    }

    static class TrailPoint {
        float x, y, life = 1f;
        TrailPoint(float x, float y) { this.x = x; this.y = y; }
    }

    static class Fruit {
        float x, y, vx, vy, gravity, r, rot, spin;
        int type;
        boolean bomb;
        void update(float dt) { x += vx * dt; y += vy * dt; vy += gravity * dt; rot += spin * dt; }
        int color() {
            int[] colors = {0xffef4444, 0xffffcc33, 0xff22c55e, 0xffff7a18, 0xffa855f7};
            return colors[type % colors.length];
        }
        void draw(Canvas c, Paint p) {
            c.save(); c.translate(x, y); c.rotate(rot * 57.2958f);
            p.setStyle(Paint.Style.FILL);
            if (bomb) drawBomb(c, p); else drawFruit(c, p);
            c.restore();
        }
        void drawFruit(Canvas c, Paint p) {
            p.setColor(color());
            if (type == 2) {
                c.drawOval(new RectF(-r, -r * 0.78f, r, r * 0.78f), p);
                p.setColor(0xff14532d); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(6f);
                c.drawArc(new RectF(-r * 0.75f, -r * 0.62f, r * 0.75f, r * 0.62f), 200, 140, false, p);
                c.drawArc(new RectF(-r * 0.45f, -r * 0.62f, r * 0.45f, r * 0.62f), 200, 140, false, p);
                p.setStyle(Paint.Style.FILL);
            } else {
                c.drawOval(new RectF(-r, -r * 0.82f, r, r * 0.82f), p);
            }
            p.setColor(0x55ffffff);
            c.drawCircle(-r * 0.32f, -r * 0.25f, r * 0.20f, p);
            p.setColor(0x77000000);
            c.drawArc(new RectF(-r, -r * 0.82f, r, r * 0.82f), 25, 100, true, p);
            p.setColor(0xff166534);
            c.drawOval(new RectF(r * 0.05f, -r * 1.22f, r * 0.65f, -r * 0.72f), p);
            p.setColor(0xff7c2d12);
            c.drawRect(-4, -r * 1.07f, 4, -r * 0.70f, p);
        }
        void drawBomb(Canvas c, Paint p) {
            RadialGradient g = new RadialGradient(-r * .25f, -r * .3f, r * 1.2f, 0xff4b5563, 0xff050505, Shader.TileMode.CLAMP);
            p.setShader(g); c.drawCircle(0, 0, r, p); p.setShader(null);
            p.setColor(0xff9ca3af); c.drawCircle(-r * 0.25f, -r * 0.28f, r * 0.18f, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(7f); p.setColor(0xffffdd55);
            c.drawArc(new RectF(-r * 0.2f, -r * 1.35f, r * 1.25f, -r * 0.25f), 190, 115, false, p);
            p.setStyle(Paint.Style.FILL); p.setColor(0xffff7a18); c.drawCircle(r * 0.78f, -r * 1.05f, r * 0.14f, p);
        }
    }

    static class Piece {
        float x, y, vx, vy, r, rot, spin, life = 1.35f;
        int side, type, color;
        void update(float dt) { x += vx * dt; y += vy * dt; vy += 860f * dt; rot += spin * dt; life -= dt; }
        void draw(Canvas c, Paint p) {
            c.save(); c.translate(x, y); c.rotate(rot * 57.2958f);
            p.setStyle(Paint.Style.FILL); p.setColor((Math.max(0, Math.min(255, (int)(life / 1.35f * 255))) << 24) | (color & 0x00ffffff));
            RectF oval = side < 0 ? new RectF(-r, -r * .78f, 0, r * .78f) : new RectF(0, -r * .78f, r, r * .78f);
            c.drawOval(oval, p);
            p.setColor(0x66ffffff); c.drawCircle(side < 0 ? -r * .35f : r * .35f, -r * .22f, r * .14f, p);
            c.restore();
        }
    }

    static class Particle {
        float x, y, vx, vy, r, life;
        int color;
        void update(float dt) { x += vx * dt; y += vy * dt; vy += 520f * dt; life -= dt; }
        void draw(Canvas c, Paint p) {
            int a = Math.max(0, Math.min(255, (int)(life * 360f)));
            p.setStyle(Paint.Style.FILL); p.setColor((a << 24) | (color & 0x00ffffff));
            c.drawCircle(x, y, r, p);
        }
    }
}
