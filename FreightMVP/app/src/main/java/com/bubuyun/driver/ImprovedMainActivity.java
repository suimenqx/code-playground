package com.bubuyun.driver;

import android.content.*;
import android.net.Uri;
import android.graphics.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

public class ImprovedMainActivity extends MainActivity {
    String avatarUri = "";

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

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams avatarLp = new LinearLayout.LayoutParams(dp(66), dp(66));
        avatarLp.setMargins(0, 0, dp(12), 0);
        top.addView(profileAvatarView(), avatarLp);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        nameRow.addView(tv(driverName, 22, deep, Typeface.BOLD), new LinearLayout.LayoutParams(-2, -2));
        LinearLayout.LayoutParams verifiedLp = new LinearLayout.LayoutParams(dp(64), dp(28));
        verifiedLp.setMargins(dp(6), 0, 0, 0);
        nameRow.addView(verifiedNameTag(), verifiedLp);
        info.addView(nameRow);

        info.addView(tv(driverCompany, 14, muted, 0));
        info.addView(tv(driverVehicle + "｜核载 " + driverCapacity, 14, muted, 0));
        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        TextView edit = tv("编辑", 13, blue, Typeface.BOLD);
        edit.setGravity(Gravity.CENTER);
        edit.setBackground(st(Color.rgb(239, 246, 255), 16, 1, Color.rgb(197, 215, 247)));
        edit.setOnClickListener(v -> editDriver());
        top.addView(edit, new LinearLayout.LayoutParams(dp(64), dp(36)));
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
