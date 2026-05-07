package com.bubuyun.driver;

import android.content.*;
import android.net.Uri;
import android.graphics.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

public class ImprovedMainActivity extends MainActivity {
    String avatarUri = "";
    long profileTapAt = 0;

    View profileAvatarView() {
        avatarUri = get("avatar", "");
        if (avatarUri != null && avatarUri.length() > 0) {
            ImageView img = new ImageView(this);
            img.setImageURI(Uri.parse(avatarUri));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setBackground(st(Color.rgb(230, 236, 247), 30, 1, Color.rgb(205, 216, 235)));
            img.setOnClickListener(v -> pickAvatar());
            return img;
        }
        TextView a = tv(driverName != null && driverName.length() > 0 ? driverName.substring(0, 1) : "司", 24, Color.WHITE, Typeface.BOLD);
        a.setGravity(Gravity.CENTER);
        a.setBackground(sh(blue, 30));
        a.setOnClickListener(v -> pickAvatar());
        return a;
    }

    void pickAvatar() {
        Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        it.addCategory(Intent.CATEGORY_OPENABLE);
        it.setType("image/*");
        startActivityForResult(it, 8);
    }

    void bindDoubleTapEdit(View v) {
        v.setOnClickListener(x -> {
            long now = System.currentTimeMillis();
            if (now - profileTapAt < 420) editDriver();
            profileTapAt = now;
        });
    }

    TextView verifiedNameTag() {
        TextView tag = tv("已认证", 12, green, Typeface.BOLD);
        tag.setGravity(Gravity.CENTER);
        tag.setSingleLine(true);
        tag.setBackground(st(Color.rgb(236, 253, 245), 12, 1, green));
        return tag;
    }

    TextView certPill(String text, int color) {
        TextView b = tv("✓ " + text, 12, color, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(true);
        b.setBackground(st(Color.WHITE, 14, 1, color));
        return b;
    }

    void addCertificationRow(LinearLayout parent) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(2), dp(12), dp(2), dp(4));
        String[] labels = {"实名认证", "车辆认证", "平台优选"};
        int[] colors = {green, blue, orange};
        for (int i = 0; i < labels.length; i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(36), 1);
            lp.setMargins(dp(3), 0, dp(3), 0);
            row.addView(certPill(labels[i], colors[i]), lp);
        }
        parent.addView(row);
    }

    TextView truckPlate() {
        TextView p = tv(plateNo, 18, Color.BLACK, Typeface.BOLD);
        p.setGravity(Gravity.CENTER);
        p.setSingleLine(true);
        p.setLetterSpacing(0.08f);
        p.setBackground(st(Color.rgb(255, 205, 35), 8, 2, Color.BLACK));
        return p;
    }

    void addPlateBlock(LinearLayout parent) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackground(st(Color.rgb(250, 252, 255), 14, 1, Color.rgb(226, 233, 246)));
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(-1, -2);
        boxLp.setMargins(0, dp(10), 0, dp(4));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView label = tv("牵引车号牌", 13, muted, 0);
        label.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(label, new LinearLayout.LayoutParams(0, dp(42), 1));
        row.addView(truckPlate(), new LinearLayout.LayoutParams(dp(154), dp(42)));
        box.addView(row);
        parent.addView(box, boxLp);
    }

    void bindSwipeBack(View v) {
        final float[] sx = new float[1], sy = new float[1];
        final boolean[] swiping = new boolean[1];
        v.setOnTouchListener((view, e) -> {
            int a = e.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                sx[0] = e.getX();
                sy[0] = e.getY();
                swiping[0] = false;
                return true;
            }
            if (a == MotionEvent.ACTION_MOVE) {
                float dx = e.getX() - sx[0], dy = e.getY() - sy[0];
                if (Math.abs(dx) > dp(10) && Math.abs(dx) > Math.abs(dy) * 1.4f) {
                    swiping[0] = true;
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return swiping[0];
            }
            if (a == MotionEvent.ACTION_UP) {
                float dx = e.getX() - sx[0], dy = e.getY() - sy[0];
                view.getParent().requestDisallowInterceptTouchEvent(false);
                if (swiping[0] && dx < -dp(64) && Math.abs(dx) > Math.abs(dy) * 1.4f) {
                    showMe();
                    return true;
                }
                if (!swiping[0] && Math.abs(dx) < dp(8) && Math.abs(dy) < dp(8)) return false;
                return swiping[0];
            }
            if (a == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
                return swiping[0];
            }
            return false;
        });
    }

    @Override
    void initData() {
        super.initData();
        def("avatar", "");
    }

    @Override
    void load() {
        super.load();
        avatarUri = get("avatar", "");
    }

    @Override
    void showMe() {
        screen = "me";
        load();
        clear("我的");
        header("我的", "司机中心");

        LinearLayout profile = card();
        bindDoubleTapEdit(profile);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(dp(66), dp(66));
        avatarLp.setMargins(0, 0, dp(12), 0);
        top.addView(profileAvatarView(), avatarLp);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        bindDoubleTapEdit(info);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        bindDoubleTapEdit(nameRow);
        nameRow.addView(tv(driverName, 22, deep, Typeface.BOLD), new LinearLayout.LayoutParams(-2, -2));
        LinearLayout.LayoutParams verifiedLp = new LinearLayout.LayoutParams(dp(64), dp(28));
        verifiedLp.setMargins(dp(6), 0, 0, 0);
        nameRow.addView(verifiedNameTag(), verifiedLp);
        info.addView(nameRow);

        info.addView(tv(driverCompany, 14, muted, 0));
        info.addView(tv(driverVehicle + "｜核载 " + driverCapacity, 14, muted, 0));
        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));
        profile.addView(top);

        addCertificationRow(profile);
        addPlateBlock(profile);
        profile.addView(tv("手机号：" + driverPhone, 15, muted, 0));

        LinearLayout walletCard = card();
        walletCard.addView(tv("账户余额", 14, muted, 0));
        walletCard.addView(money(wallet, 30, orange, Typeface.BOLD, "wallet"));

        title("我的运单");
        billPills("全部", "待装货", "运输中", "已完成");
        int shown = 0;
        for (Waybill w : bills) {
            if ("全部".equals(billFilter) || w.status.equals(billFilter)) {
                billCard(w);
                shown++;
            }
        }
        if (shown == 0) content.addView(tv("暂无" + ("全部".equals(billFilter) ? "运单" : billFilter + "运单"), 15, muted, 0));
    }

    @Override
    void waybillDetail(Waybill w) {
        screen = "billDetail";
        clear("我的");
        header("运单详情", w.no);
        bindSwipeBack(content);

        LinearLayout sb = card();
        bindSwipeBack(sb);
        LinearLayout sr = new LinearLayout(this);
        sr.setOrientation(LinearLayout.HORIZONTAL);
        sb.addView(sr);
        chip(sr, w, "待装货");
        chip(sr, w, "运输中");
        chip(sr, w, "已完成");

        LinearLayout d = card();
        bindSwipeBack(d);
        d.addView(tv(w.status + "｜" + w.c.from + " → " + w.c.to, 20, blue, Typeface.BOLD));
        row(d, "货主", w.c.owner + "｜" + w.c.company);
        row(d, "联系电话", w.c.phone);
        row(d, "货物", w.c.goods);
        row(d, "车辆要求", w.c.vehicle);
        row(d, "运费", w.c.price);
        row(d, "结算方式", w.c.settle);
        row(d, "收货方", w.c.receiver);
        row(d, "备注", w.c.note);

        sec("榜单信息");
        LinearLayout bill = card();
        bindSwipeBack(bill);
        row(bill, "装货位置", w.c.load);
        row(bill, "装货吨数", w.c.weight);
        row(bill, "装货时间", w.c.time);
        Button up = lbtn(w.img == null ? "上传本地榜单图片" : "更换榜单图片");
        up.setOnClickListener(v -> {
            pending = w;
            Intent it = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            it.addCategory(Intent.CATEGORY_OPENABLE);
            it.setType("image/*");
            startActivityForResult(it, 7);
        });
        bill.addView(up, new LinearLayout.LayoutParams(-1, dp(44)));
        if (w.img != null) {
            ImageView im = new ImageView(this);
            im.setImageURI(w.img);
            im.setScaleType(ImageView.ScaleType.CENTER_CROP);
            im.setOnClickListener(v -> showImage(w.img));
            bill.addView(im, new LinearLayout.LayoutParams(-1, dp(190)));
        }

        sec("运输信息");
        LinearLayout t = card();
        bindSwipeBack(t);
        row(t, "承运司机", driverName);
        row(t, "车牌号", plateNo);
        row(t, "司机电话", driverPhone);
        row(t, "车辆信息", driverVehicle + "｜" + driverCapacity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 8 && resultCode == RESULT_OK && data != null) {
            Uri u = data.getData();
            avatarUri = u.toString();
            try { getContentResolver().takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception e) {}
            set("avatar", avatarUri);
            Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show();
            showMe();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
