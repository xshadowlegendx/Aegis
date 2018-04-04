package me.impy.aegis.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.amulyakhare.textdrawable.TextDrawable;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import me.impy.aegis.R;
import me.impy.aegis.crypto.KeyInfo;
import me.impy.aegis.crypto.KeyInfoException;
import me.impy.aegis.db.DatabaseEntry;
import me.impy.aegis.encoding.Base32;
import me.impy.aegis.helpers.EditTextHelper;
import me.impy.aegis.helpers.SpinnerHelper;
import me.impy.aegis.helpers.TextDrawableHelper;
import me.impy.aegis.ui.views.KeyProfile;

public class EditProfileActivity extends AegisActivity {
    private boolean _isNew = false;
    private boolean _edited = false;
    private KeyProfile _profile;

    private ImageView _iconView;

    private EditText _textName;
    private EditText _textIssuer;
    private EditText _textPeriod;
    private EditText _textSecret;

    private Spinner _spinnerType;
    private Spinner _spinnerAlgo;
    private Spinner _spinnerDigits;
    private SpinnerItemSelectedListener _selectedListener = new SpinnerItemSelectedListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ActionBar bar = getSupportActionBar();
        bar.setHomeAsUpIndicator(R.drawable.ic_close);
        bar.setDisplayHomeAsUpEnabled(true);

        // if the intent doesn't contain a KeyProfile, create a new one
        Intent intent = getIntent();
        _profile = (KeyProfile) intent.getSerializableExtra("KeyProfile");
        _isNew = intent.getBooleanExtra("isNew", false);
        if (_profile == null) {
            _profile = new KeyProfile();
        }
        if (_isNew) {
            setTitle("Add profile");
        }

        _iconView = findViewById(R.id.profile_drawable);
        _textName = findViewById(R.id.text_name);
        _textIssuer = findViewById(R.id.text_issuer);
        _textPeriod = findViewById(R.id.text_period);
        _textSecret = findViewById(R.id.text_secret);
        _spinnerType = findViewById(R.id.spinner_type);
        SpinnerHelper.fillSpinner(this, _spinnerType, R.array.otp_types_array);
        _spinnerAlgo = findViewById(R.id.spinner_algo);
        SpinnerHelper.fillSpinner(this, _spinnerAlgo, R.array.otp_algo_array);
        _spinnerDigits = findViewById(R.id.spinner_digits);
        SpinnerHelper.fillSpinner(this, _spinnerDigits, R.array.otp_digits_array);

        updateFields();

        _textName.addTextChangedListener(_textListener);
        _textIssuer.addTextChangedListener(_textListener);
        _textPeriod.addTextChangedListener(_textListener);
        _textSecret.addTextChangedListener(_textListener);
        _spinnerType.setOnTouchListener(_selectedListener);
        _spinnerType.setOnItemSelectedListener(_selectedListener);
        _spinnerAlgo.setOnTouchListener(_selectedListener);
        _spinnerAlgo.setOnItemSelectedListener(_selectedListener);
        _spinnerDigits.setOnTouchListener(_selectedListener);
        _spinnerDigits.setOnItemSelectedListener(_selectedListener);

        // update the icon if the text changed
        _textName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                TextDrawable drawable = TextDrawableHelper.generate(s.toString());
                _iconView.setImageDrawable(drawable);
            }
        });


        RelativeLayout expandableLayout = findViewById(R.id.expandableLayout);
        RelativeLayout header=(RelativeLayout) findViewById(R.id.accordian_header);

//to toggle content
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation fadeOut = new AlphaAnimation(1, 0);  // the 1, 0 here notifies that we want the opacity to go from opaque (1) to transparent (0)
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(220); // Fadeout duration should be 1000 milli seconds
                header.startAnimation(fadeOut);

                Animation fadeIn = new AlphaAnimation(0, 1);  // the 1, 0 here notifies that we want the opacity to go from opaque (1) to transparent (0)
                fadeIn.setInterpolator(new AccelerateInterpolator());
                fadeIn.setDuration(250); // Fadeout duration should be 1000 milli seconds

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        header.setVisibility(View.GONE);
                        expandableLayout.startAnimation(fadeIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        expandableLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    private void updateFields() {
        DatabaseEntry entry = _profile.getEntry();
        _iconView.setImageDrawable(_profile.getDrawable());

        _textName.setText(entry.getName());
        _textIssuer.setText(entry.getInfo().getIssuer());
        _textPeriod.setText(Integer.toString(entry.getInfo().getPeriod()));

        byte[] secretBytes = entry.getInfo().getSecret();
        if (secretBytes != null) {
            char[] secretChars = Base32.encode(secretBytes);
            _textSecret.setText(secretChars, 0, secretChars.length);
        }

        String type = entry.getInfo().getType();
        _spinnerType.setSelection(getStringResourceIndex(R.array.otp_types_array, type), false);

        String algo = entry.getInfo().getAlgorithm(false);
        _spinnerAlgo.setSelection(getStringResourceIndex(R.array.otp_algo_array, algo), false);

        String digits = Integer.toString(entry.getInfo().getDigits());
        _spinnerDigits.setSelection(getStringResourceIndex(R.array.otp_digits_array, digits), false);
    }

    @Override
    protected void setPreferredTheme(boolean nightMode) {
        if (nightMode) {
            setTheme(R.style.AppTheme_Dark_TransparentActionBar);
        } else {
            setTheme(R.style.AppTheme_Default_TransparentActionBar);
        }
    }

    @Override
    public void onBackPressed() {
        if (!_edited) {
            super.onBackPressed();
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage("Your changes have not been saved")
                .setPositiveButton(R.string.save, (dialog, which) -> onSave())
                .setNegativeButton(R.string.discard, (dialog, which) -> super.onBackPressed())
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                return onSave();
            case R.id.action_delete:
                return onDelete();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        if (_isNew) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    private void finish(boolean delete) {
        Intent intent = new Intent();
        intent.putExtra("KeyProfile", _profile);
        intent.putExtra("delete", delete);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean onDelete() {
        finish(true);
        return true;
    }

    private boolean onSave() {
        if (_textSecret.length() == 0) {
            onError("Secret is a required field.");
            return false;
        }

        int period;
        try {
            period = Integer.parseInt(_textPeriod.getText().toString());
        } catch (NumberFormatException e) {
            onError("Period is not an integer.");
            return false;
        }

        String type = _spinnerType.getSelectedItem().toString();
        String algo = _spinnerAlgo.getSelectedItem().toString();

        int digits;
        try {
            digits = Integer.parseInt(_spinnerDigits.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            onError("Digits is not an integer.");
            return false;
        }

        DatabaseEntry entry = _profile.getEntry();
        entry.setName(_textName.getText().toString());
        KeyInfo info = entry.getInfo();

        try {
            char[] secret = EditTextHelper.getEditTextChars(_textSecret);
            info.setSecret(secret);
            info.setIssuer(_textIssuer.getText().toString());
            info.setPeriod(period);
            info.setDigits(digits);
            info.setAlgorithm(algo);
            info.setType(type);
        } catch (KeyInfoException e) {
            onError("The entered info is incorrect: " + e.getMessage());
            return false;
        }

        finish(false);
        return true;
    }

    private void onError(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Error saving profile")
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void onFieldEdited() {
        _edited = true;
    }

    private TextWatcher _textListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            onFieldEdited();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onFieldEdited();
        }

        @Override
        public void afterTextChanged(Editable s) {
            onFieldEdited();
        }
    };

    private class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        private boolean _userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            _userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (_userSelect) {
                onFieldEdited();
                _userSelect = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private int getStringResourceIndex(@ArrayRes int id, String string) {
        String[] res = getResources().getStringArray(id);
        for (int i = 0; i < res.length; i++) {
            if (res[i].equals(string)) {
                return i;
            }
        }
        return -1;
    }
}