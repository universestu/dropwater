package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final MyGdxGame game;

    Texture dropImage;
    Texture bucketImage;
    Sound dropSound;
    Music rainMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Rectangle> raindrops;
    long lastDropTime;
    int dropsGathered;

    public GameScreen(final MyGdxGame gam) {
        this.game = gam;

        // โหลดไฟล์รูปถังน้ำและเม็ดฝน ขนาด 64x64
        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // โหลดเสียงเม็ดฝน และ effect
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);

        // สร้าง camera และ SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // สร้างออปเจ็ค bucket จากคลาส Rectangle
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;


        // สร้าง array เม็ดฝน และเริ่มโปรยเม็ดฝนเม็ดแรก
        raindrops = new Array<Rectangle>();
        spawnRaindrop();

    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800 - 64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        // เคลียร์หน้าจอ ด้วยสี Dark Blue
        // โดย argument ของ glClearColor คือ red, green, blue, alpha
        // ค่าอยู่ระหว่าง [0, 1] (float)
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // camera อัพเดท matrix
        camera.update();

        // ให้ SpriteBatch ทำการเรนเดอร์พิกัดทั้งหมดของระบบ
        game.batch.setProjectionMatrix(camera.combined);

        // เริ่มวาด เรนเดอร์ ตะกร้าและเม็ดฝน รวมถึงนับจำนวนเม็ดฝนที่เก็บได้
        game.batch.begin();
        game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 480);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop : raindrops) {
            game.batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        game.batch.end();

        //Animation spriteHuman = new Animation();
        //spriteHuman.create();
       // spriteHuman.render();

        // เช็คว่า มีการคลิกเมาท์หรือแตะหน้าจอหรือไม่
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
            bucket.y = touchPos.y - 64 / 2;
        }

        // เช็คว่า มีการกดคีย์บอร์ดปุ่มลูกศรซ้าย/ขวา หรือไม่
        if (Gdx.input.isKeyPressed(Keys.LEFT))
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.RIGHT))
            bucket.x += 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.UP))
            bucket.y += 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.DOWN))
            bucket.y -= 200 * Gdx.graphics.getDeltaTime();

        // เช็คไม่ให้ถังน้ำมันล้นหน้าจอ
        if (bucket.x < 0)
            bucket.x = 0;
        if (bucket.x > 800 - 64)
            bucket.x = 800 - 64;
        if (bucket.y < 0)
            bucket.y = 0;
        if (bucket.y > 480 - 64)
            bucket.y = 480 - 64;

        // เช็คว่าถึงเวลาที่จะโปรยเม็ดฝนเม็ดถัดไปหรือยัง
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
            spawnRaindrop();

        // ทำให้เม็ดฝนขยับ
        // โดยลบเม็ดฝนทุกครั้งที่ตกลงพ้นขอบจอหรือว่าไปชนกับถังน้ำ
        // รวมถึงให้มันเล่นเสียงเวลาโดนถังน้ำ
        Iterator<Rectangle> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + 64 < 0)
                iter.remove();
            if (raindrop.overlaps(bucket)) {
                dropsGathered++;
                dropSound.play();
                iter.remove();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // เริ่มเล่นเสียงเพลง (เสียงฝนตก) เมื่อหน้าจอนี้แสดง
        rainMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
