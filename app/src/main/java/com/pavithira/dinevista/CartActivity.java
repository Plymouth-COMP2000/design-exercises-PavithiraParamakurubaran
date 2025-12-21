package com.pavithira.dinevista;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.pavithira.dinevista.models.CartItem;
import com.pavithira.dinevista.models.Reservation;

import java.util.ArrayList;
import java.util.Calendar;

public class CartActivity extends BaseActivity {

    private LinearLayout cartContainer;
    private TextView txtSubtotal, txtDate, txtTime;
    private EditText etFullName, etEmail, etPhone, etSpecialRequest;
    private TextView tvPax;

    private Button btnProceed;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        setupBottomNavigation(R.id.nav_cart);
        setupDrawer();

        cartContainer = findViewById(R.id.cartItemsContainer);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);
        btnProceed = findViewById(R.id.btnProceed);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSpecialRequest = findViewById(R.id.etSpecialRequest);
        tvPax = findViewById(R.id.tvPax);
        tvPax.setOnClickListener(v -> showPaxDialog());
        RadioGroup paymentGroup = findViewById(R.id.paymentGroup);


        loadCartItems();
        setupDatePicker();
        setupTimePicker();


        // Receive cart items from reservation (edit mode)
        ArrayList<CartItem> cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
        if (cartItems != null) {
            CartManager.getInstance().clearCart();
            CartManager.getInstance().getItems().addAll(cartItems);
        }

// Pre-fill reservation details if passed
        String name = getIntent().getStringExtra("name");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String pax = getIntent().getStringExtra("pax");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String request = getIntent().getStringExtra("request");

        if (name != null) etFullName.setText(name);
        if (email != null) etEmail.setText(email);
        if (phone != null) etPhone.setText(phone);
        if (pax != null) tvPax.setText(pax);
        if (date != null) txtDate.setText(date);
        if (time != null) txtTime.setText(time);
        if (request != null) etSpecialRequest.setText(request);


        btnProceed.setOnClickListener(v -> goToPreview());
    }

    private void loadCartItems() {
        cartContainer.removeAllViews();

        for (CartItem item : CartManager.getInstance().getItems()) {

            View row = getLayoutInflater()
                    .inflate(R.layout.row_cart_item, cartContainer, false);

            TextView txtName = row.findViewById(R.id.txtItemName);
            TextView txtQty = row.findViewById(R.id.txtQty);
            TextView txtPrice = row.findViewById(R.id.txtPrice);
            ImageButton btnPlus = row.findViewById(R.id.btnPlus);
            ImageButton btnMinus = row.findViewById(R.id.btnMinus);

            txtName.setText(item.getName());
            txtQty.setText(String.valueOf(item.getQty()));
            txtPrice.setText("RM " + item.getTotalPrice());

            btnPlus.setOnClickListener(v -> {
                item.increaseQty();
                refreshCart();
            });

            btnMinus.setOnClickListener(v -> {
                if (item.getQty() > 1) {
                    item.decreaseQty();
                } else {
                    CartManager.getInstance().removeItem(item);
                }
                refreshCart();
            });

            cartContainer.addView(row);
        }

        updateSubtotal();
    }

    private void refreshCart() {
        loadCartItems();
    }

    private void updateSubtotal() {
        txtSubtotal.setText("Subtotal  RM " +
                String.format("%.2f", CartManager.getInstance().getSubtotal()));
    }

    private void setupDatePicker() {
        txtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, y, m, d) ->
                            txtDate.setText(d + "/" + (m + 1) + "/" + y),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupTimePicker() {
        txtTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this,
                    (view, h, m) ->
                            txtTime.setText(h + ":" + String.format("%02d", m)),
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true).show();
        });
    }

    private void showPaxDialog() {
        String[] paxOptions = {"1 pax", "2 pax", "3 pax", "4 pax", "5 pax", "6 pax"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Number of Pax");
        builder.setItems(paxOptions, (dialog, which) -> {
            tvPax.setText(paxOptions[which]);
        });
        builder.show();
    }
    public static Intent createIntent(Context context, Reservation reservation) {
        Intent intent = new Intent(context, CartActivity.class);
        intent.putExtra("reservationId", reservation.id);
        intent.putExtra("name", reservation.name);
        intent.putExtra("email", reservation.email);
        intent.putExtra("phone", reservation.phone);
        intent.putExtra("pax", reservation.pax);
        intent.putExtra("date", reservation.date);
        intent.putExtra("time", reservation.time);
        intent.putExtra("request", reservation.request);
        intent.putExtra("subtotal", reservation.subtotal);
        intent.putExtra("cartItems", new java.util.ArrayList<>(reservation.items));
        return intent;
    }

    private void goToPreview() {

        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pax = tvPax.getText().toString();
        String date = txtDate.getText().toString();
        String time = txtTime.getText().toString();
        String request = etSpecialRequest.getText().toString();

        if (name.isEmpty() || phone.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioGroup paymentGroup = findViewById(R.id.paymentGroup);
        int selectedId = paymentGroup.getCheckedRadioButtonId();
        String paymentMethod = "Cash"; // default

        if (selectedId == R.id.rbGooglePay) paymentMethod = "Google Pay";
        else if (selectedId == R.id.rbApplePay) paymentMethod = "Apple Pay";
        else if (selectedId == R.id.rbCreditCard) paymentMethod = "Credit Card";
        else if (selectedId == R.id.rbCash) paymentMethod = "Cash";

        Intent intent = new Intent(this, PreviewReservationActivity.class);

        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("pax", pax);
        intent.putExtra("date", date);
        intent.putExtra("time", time);
        intent.putExtra("request", request);
        intent.putExtra("subtotal", CartManager.getInstance().getSubtotal());
        intent.putExtra("paymentMethod", paymentMethod);

        // Pass editing info if available
        String reservationId = getIntent().getStringExtra("reservationId");
        if (reservationId != null) {
            intent.putExtra("isEdit", true);
            intent.putExtra("reservationId", reservationId);
        }

        startActivity(intent);
    }



}
