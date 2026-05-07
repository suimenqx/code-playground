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

    TextView badge(String text, int color) {
        TextView b = tv("✓ " + text, 12, color, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setBackground(st(Color.WHITE, 12, 1, color));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(28));
        lp.setMargins(0, dp(4), dp(6), 0);
        b.setLayoutParams(lp);
        return b;
    }

    TextView truckPlate() {
        TextView p = tv(plateNo, 18, Color.BLACK, Typeface.BOLD);
        p.setGravity(Gravity.CENTER);
        p.setLetterSpacing(0.08f);
        p.setBackground(st(Color.rgb(255, 205, 35), 8, 2, Color.BLACK));
        return p;
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
        top.addView(profileAvatarView(), new LinearLayout.LayoutParams(dp(66), dp(66)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(tv(driverName, 22, deep, Typeface.BOLD));
        info.addView(tv(driverCompany + "｜" + driverVehicle + "｜载重 " + driverCapacity, 14, muted, 0));
        LinearLayout badges = new LinearLayout(this);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        badges.addView(badge("实名认证", green));
        badges.addView(badge("车辆认证", blue));
        badges.addView(badge("平台优选", orange));
        info.addView(badges);
        top.addView(info, new LinearLayout.LayoutParams(0, -2, 1));

        TextView edit = tv("编辑", 13, blue, Typeface.BOLD);
        edit.setGravity(Gravity.CENTER);
        edit.setBackground(st(Color.rgb(239, 246, 255), 16, 1, Color.rgb(197, 215, 247)));
        edit.setOnClickListener(v -> editDriver());
        top.addView(edit, new LinearLayout.LayoutParams(dp(64), dp(36)));
        profile.addView(top);

        LinearLayout plateRow = new LinearLayout(this);
        plateRow.setGravity(Gravity.CENTER_VERTICAL);
        plateRow.setPadding(dp(8), dp(8), dp(8), dp(4));
        plateRow.addView(tv("牵引车号牌", 13, muted, 0));
        LinearLayout.LayoutParams plateLp = new LinearLayout.LayoutParams(dp(146), dp(42));
        plateLp.setMargins(dp(8), 0, 0, 0);
        plateRow.addView(truckPlate(), plateLp);
        profile.addView(plateRow);
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
