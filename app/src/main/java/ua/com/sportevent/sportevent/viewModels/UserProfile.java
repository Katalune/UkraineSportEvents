package ua.com.sportevent.sportevent.viewModels;

import android.app.DatePickerDialog;
import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.facebook.Profile;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ua.com.sportevent.sportevent.BR;
import ua.com.sportevent.sportevent.R;
import ua.com.sportevent.sportevent.helpers.StorageHelper;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.serverData.ServerContract;

/**
 * Stores information about the user.
 */
public class UserProfile  extends BaseObservable {
    // They are static so to preserve changes across screen rotation.
    static Set<String> updatedFields = new HashSet<>();
    private static Set<String> requiredEmptyFields = new HashSet<>();

    private boolean filled = false;
    private Locale mLocale;

    private static String gender = "-1";
    static final String posMale = "1";
    static final String posFemale = "2";

    private Date birthDate = new Date();

    private static String tshirtSize = "-1";
    public static final String posXS = "XS";
    public static final String posS = "S";
    public static final String posM = "M";
    public static final String posL = "L";
    public static final String posXL = "XL";
    public static final String posXXL = "XXL";

    public final ObservableField<String> firstNameLabel = new ObservableField<>("");
    public final ObservableField<String> fatherNameLabel = new ObservableField<>("");
    public final ObservableField<String> lastNameLabel = new ObservableField<>("");
    public final ObservableField<String> genderLabel = new ObservableField<>("");
    public final ObservableField<String> birthDayLabel = new ObservableField<>("");
    public final ObservableField<String> phoneLabel = new ObservableField<>("");
    public final ObservableField<String> phone2Label = new ObservableField<>("");
    public final ObservableField<String> sizeLabel = new ObservableField<>("");
    public final ObservableField<String> clubLabel = new ObservableField<>("");
    public final ObservableField<String> countryLabel = new ObservableField<>("");
    public final ObservableField<String> regionLabel = new ObservableField<>("");
    public final ObservableField<String> cityLabel = new ObservableField<>("");
    public final ObservableField<String> aboutLabel = new ObservableField<>("");

    public final ObservableField<String> firstName = new ObservableField<>("");
    public final ObservableField<String> fatherName = new ObservableField<>("");
    public final ObservableField<String> lastName = new ObservableField<>("");
    public final ObservableField<String> phoneNumber = new ObservableField<>("");
    public final ObservableField<String> phoneNumber2 = new ObservableField<>("");
    public final ObservableField<String> club = new ObservableField<>("");
    public final ObservableField<String> country = new ObservableField<>("");
    public final ObservableField<String> region = new ObservableField<>("");
    public final ObservableField<String> city = new ObservableField<>("");
    public final ObservableField<String> about = new ObservableField<>("");
    public final ObservableField<Uri> loadAvatar = new ObservableField<>();

    public final ProfileField firstNameField = new ProfileField(ServerContract.ProfileEntry.FIRST_NAME, firstName, firstNameLabel, true);
    public final ProfileField fatherNameField = new ProfileField(ServerContract.ProfileEntry.FATHER_NAME, fatherName, fatherNameLabel);
    public final ProfileField lastNameField = new ProfileField(ServerContract.ProfileEntry.LAST_NAME, lastName, lastNameLabel, true);
    public final ProfileField genderField = new ProfileField(ServerContract.ProfileEntry.SEX, genderLabel, true);
    public final ProfileField birthDayField = new ProfileField(ServerContract.ProfileEntry.BDAY, birthDayLabel, true);
    public final ProfileField phoneNumberField = new ProfileField(ServerContract.ProfileEntry.PHONE, phoneNumber, phoneLabel, true);
    public final ProfileField phoneNumber2Field = new ProfileField(ServerContract.ProfileEntry.PHONE_2, phoneNumber2, phone2Label, true);
    public final ProfileField sizeField = new ProfileField(ServerContract.ProfileEntry.TSHIRT_SIZE, sizeLabel, true);
    public final ProfileField clubField = new ProfileField(ServerContract.ProfileEntry.CLUB, club, clubLabel);
    public final ProfileField countryField = new ProfileField(ServerContract.ProfileEntry.COUNTRY, country, countryLabel, true);
    public final ProfileField regionField = new ProfileField(ServerContract.ProfileEntry.REGION, region, regionLabel, true);
    public final ProfileField cityField = new ProfileField(ServerContract.ProfileEntry.CITY, city, cityLabel, true);
    public final ProfileField aboutField = new ProfileField(ServerContract.ProfileEntry.ABOUT, about, aboutLabel);

    public static Set<String> getUpdatedFields() {
        return updatedFields;
    }

    public static boolean existEmptyRequiredFields() {
        return requiredEmptyFields.size() > 0;
    }

    /**
     * Clear info about updatedFields and notify ALL layout fields to update its state instantly.
     * Recommended to use after information upload to server - as we have not another way to
     * update info.
     */
    public void clearUpdatedFieldsAndNotify() {
        updatedFields.clear();
        firstNameField.updateEditedLabel();
        fatherNameField.updateEditedLabel();
        lastNameField.updateEditedLabel();
        phoneNumberField.updateEditedLabel();
        phoneNumber2Field.updateEditedLabel();
        clubField.updateEditedLabel();
        countryField.updateEditedLabel();
        regionField.updateEditedLabel();
        cityField.updateEditedLabel();
        aboutField.updateEditedLabel();
        birthDayField.updateEditedLabel();
        genderField.updateEditedLabel();
        sizeField.updateEditedLabel();
    }

    /**
     * Clear info about updatedFields without notifying layout.
     * Recommended to use after information downloaded from the server - so we will have other way to
     * inform layout.
     */
    public static void clearUpdatedFields() {
        updatedFields.clear();
    }

    /**
     * Update the most recent profileValues (NOT initially received from the server).
     * @return If the same key-value was already there return false and don't update the values. If
     * it is new value - return true and add key to the updatedFields set.
     */
    static boolean saveProfileValues(String key, String value, Boolean isRequired) {
        updateRequiredFields(key, value, isRequired);

        if (StorageHelper.saveProfileValue(key, value)) {
            updatedFields.add(key);
            return true;
        }
        return false;
    }

    private static void updateRequiredFields(String key, String value, Boolean isRequired) {
        if (isRequired) {
            if (value.length() > 0) {
                if (requiredEmptyFields.contains(key)) {
                    requiredEmptyFields.remove(key);
                }
            } else {
                requiredEmptyFields.add(key);
            }
        }
    }

    public UserProfile(Context context) {
        mLocale = Utility.getLocale(context);
        // Set fb pic as avatar.
        int coverWidth = (int) context.getResources().getDimension(R.dimen.profile_avatar_width);
        int coverHeight = (int) context.getResources().getDimension(R.dimen.profile_avatar_height);
        if (Profile.getCurrentProfile() != null){
            loadAvatar.set(Profile.getCurrentProfile().getProfilePictureUri(coverWidth, coverHeight));
        }
        firstNameField.setLabel(context.getString(R.string.profile_firstname_hint_text));
        fatherNameField.setLabel(context.getString(R.string.profile_fathername_hint_text));
        lastNameField.setLabel(context.getString(R.string.profile_lastname_hint_text));
        phoneNumberField.setLabel(context.getString(R.string.profile_phone_hint_text));
        phoneNumber2Field.setLabel(context.getString(R.string.profile_phone2_hint_text));
        clubField.setLabel(context.getString(R.string.profile_club_hint_text));
        countryField.setLabel(context.getString(R.string.profile_country_hint_text));
        regionField.setLabel(context.getString(R.string.profile_region_hint_text));
        cityField.setLabel(context.getString(R.string.profile_city_hint_text));
        aboutField.setLabel(context.getString(R.string.profile_about_hint_text));

        genderField.setLabel(context.getString(R.string.profile_gender_hint_text));
        birthDayField.setLabel(context.getString(R.string.profile_birthday_hint_text));
        sizeField.setLabel(context.getString(R.string.profile_size_hint_text));
    }

    public boolean isFilled() {
        return filled;
    }

    @BindingAdapter({"app:addChangeListener"})
    public static void addFieldListener(View view, final ProfileField profileField) {
        if (view instanceof EditText) {
            addTextWatcher((EditText) view, profileField);
        } else if (view instanceof RadioButton) {
            addRadioClickListener((RadioButton) view, profileField);
        }
    }

    private static void addTextWatcher(EditText view, final ProfileField stringField) {
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newText = s.toString();
                try {
                    // Update profile content values and set with updated fields.
                    saveProfileValues(stringField.tag, newText, stringField.isRequired);
                    // As this value was changed - check if we need to set tilda to it.
                    stringField.updateEditedLabel();
                } catch (NullPointerException e) {
                    // We have not instance variables - nothing to do.
                }
            }
        });
    }

    private static void addRadioClickListener(RadioButton radioButton, final ProfileField radioField) {
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = view.getId();
                String pos = "";
                switch (position) {
                    case R.id.profile_position_male:
                        pos = posMale;
                        break;
                    case R.id.profile_position_woman:
                        pos = posFemale;
                        break;
                    case R.id.profile_size_xs:
                        pos = posXS;
                        break;
                    case R.id.profile_size_s:
                        pos = posS;
                        break;
                    case R.id.profile_size_m:
                        pos = posM;
                        break;
                    case R.id.profile_size_l:
                        pos = posL;
                        break;
                    case R.id.profile_size_xl:
                        pos = posXL;
                        break;
                    case R.id.profile_size_xxl:
                        pos = posXXL;
                        break;
                }
                saveProfileValues(radioField.tag, pos, true);
                radioField.updateEditedLabel();
            }
        });
    }

    @Bindable
    public String getBirthDate() {
        if (mLocale != null) {
            DateFormat bday_format = DateFormat.getDateInstance(DateFormat.LONG, mLocale);
            return bday_format.format(birthDate);
        }
        return "";
    }

    /**
     * Set the birth date by the app OR by the user via calendarToggle.
     * Because of latter we save it in profile values.
     */
    public void setBirthDate(Date birthDate) {
        if (birthDate != null) {
            this.birthDate = birthDate;
            notifyPropertyChanged(BR.birthDate);

            // Save to the static content values.
            saveProfileValues(birthDayField.tag,
                    ServerContract.ServerProfileEntry.BIRTHDAY_FORMAT.format(birthDate), true);
            birthDayField.updateEditedLabel();
        }
    }

    // Show date picker to set birth date.
    public void calendarToggle(final View v) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthDate);
        new DatePickerDialog(
                v.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newCalendar = Calendar.getInstance();
                        newCalendar.set(year, monthOfYear, dayOfMonth);
                        Date newDate = newCalendar.getTime();
                        // Update title with birthday.
                        setBirthDate(newDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Bindable
    public boolean getMale() {
        return gender.equals(posMale);
    }

    @Bindable
    public boolean getWoman() {
        return gender.equals(posFemale);
    }

    /**
     * Set the gender by the app from the profileValues. That's why we don't save it again in
     * the map here.
     */
    public void setGender(@NonNull String gender) {
        if (gender.length() > 0) {
            UserProfile.gender = gender;
            notifyPropertyChanged(BR.male);
            notifyPropertyChanged(BR.woman);

            genderField.updateEditedLabel();
        }

        updateRequiredFields(ServerContract.ProfileEntry.SEX, gender, true);
    }

    @Bindable
    public boolean getXs() {
        return tshirtSize.equals(posXS);
    }

    @Bindable
    public boolean getS() {
        return tshirtSize.equals(posS);
    }

    @Bindable
    public boolean getM() {
        return tshirtSize.equals(posM);
    }

    @Bindable
    public boolean getL() {
        return tshirtSize.equals(posL);
    }

    @Bindable
    public boolean getXl() {
        return tshirtSize.equals(posXL);
    }

    @Bindable
    public boolean getXxl() {
        return tshirtSize.equals(posXXL);
    }

    /**
     * Set the size by the app from the profileValues. That's why we don't save it again in
     * the map here.
     */
    public void setTshirtSize(@NonNull String tshirtSize) {
        if (tshirtSize.length() > 0) {
            UserProfile.tshirtSize = tshirtSize;
            notifyPropertyChanged(BR.xs);
            notifyPropertyChanged(BR.s);
            notifyPropertyChanged(BR.m);
            notifyPropertyChanged(BR.l);
            notifyPropertyChanged(BR.xl);
            notifyPropertyChanged(BR.xxl);

            sizeField.updateEditedLabel();
        }

        updateRequiredFields(ServerContract.ProfileEntry.TSHIRT_SIZE, tshirtSize, true);
    }

    // Set user info with notifying bind fields.
    public void fillUserInfo(Map<String, Object> cv, Context context) {
        String avatar = (String) cv.get(ServerContract.ProfileEntry.AVATAR);
        if (avatar != null && avatar.length() > 0) {
            int coverWidth = (int) context.getResources().getDimension(R.dimen.profile_avatar_width);
            int coverHeight = (int) context.getResources().getDimension(R.dimen.profile_avatar_height);
            Uri imgUrl = Uri.parse(ServerContract.BASE_URL_IMG).buildUpon()
                    .appendPath(String.valueOf(coverWidth))
                    .appendPath(String.valueOf(coverHeight))
                    .appendEncodedPath(avatar).build();
            // There are few levels of storage that the old image may exist in,
            // where it is most likely getting pull back in from. These expire after 60 days,
            // I believe, so it will need to be flushed also.
//        Fresco.getImagePipelineFactory().getMainDiskStorageCache().remove(new SimpleCacheKey(profilePictureUri.toString()));
//        Fresco.getImagePipeline().evictFromMemoryCache(profilePictureUri);
//        Fresco.getImagePipelineFactory().getSmallImageDiskStorageCache().remove(new SimpleCacheKey(profilePictureUri.toString()));
            loadAvatar.set(imgUrl);
        }
        // Edit Text fields
        firstNameField.setTextValue(cv);
        fatherNameField.setTextValue(cv);
        lastNameField.setTextValue(cv);
        clubField.setTextValue(cv);
        countryField.setTextValue(cv);
        regionField.setTextValue(cv);
        cityField.setTextValue(cv);
        aboutField.setTextValue(cv);
        phoneNumberField.setTextValue(cv);
        phoneNumber2Field.setTextValue(cv);

        String sex = (String) cv.get(ServerContract.ProfileEntry.SEX);
        if (sex == null) sex = "";
        setGender(sex);
        String size = (String) cv.get(ServerContract.ProfileEntry.TSHIRT_SIZE);
        if (size == null) size = "";
        setTshirtSize(size);

        String bdayStr;
        Date bdayDate = null;
        try {
            bdayStr = (String) cv.get(ServerContract.ProfileEntry.BDAY);
            bdayDate = ServerContract.ServerProfileEntry.BIRTHDAY_FORMAT.parse(bdayStr);
        } catch (Exception e) {
            Log.d("test", "fillUserInfo: ");
        }
        setBirthDate(bdayDate);
        filled = true;
    }

    public class ProfileField {
        String tag;
        ObservableField<String> textValue;
        ObservableField<String> valueLabel;
        boolean isRequired = false;
        private final String requiredSign = "*";
        private final String editedSign = "~~";
        private String textValueLabelDefault;

        /**
         * Fields of this class are default protected - so can be accessed in this package.
         * @param tag Key in the profileValues map that correspond to this key-value pair.
         * @param textValue Field text that connected to the View.
         * @param textValueLabel Label of the field that connected to the View.
         */
        public ProfileField(@NonNull String tag, @NonNull ObservableField<String> textValue,
                            @NonNull ObservableField<String> textValueLabel) {
            this.tag = tag;
            this.textValue = textValue;
            this.valueLabel = textValueLabel;
        }

        /**
         * Fields of this class are default protected - so can be accessed in this package.
         * @param tag Key in the profileValues map that correspond to this key-value pair.
         * @param textValue Field text that connected to the View.
         * @param textValueLabel Label of the field that connected to the View.
         * @param isRequired Indicate is this field required to be filled.
         */
        public ProfileField(@NonNull String tag, @NonNull ObservableField<String> textValue,
                            @NonNull ObservableField<String> textValueLabel, boolean isRequired) {
            this(tag, textValue, textValueLabel);
            this.isRequired = isRequired;
        }

        /**
         * Fields of this class are default protected - so can be accessed in this package. This
         * constructor can be used for the radio-groups - to change label text if needed.
         * @param tag Key in the profileValues map that correspond to this key-value pair.
         * @param valueLabel Label of the field that connected to the View.
         * @param isRequired Indicate is this field required to be filled.
         */
        public ProfileField(@NonNull String tag, @NonNull ObservableField<String> valueLabel, boolean isRequired) {
            this.tag = tag;
            this.valueLabel = valueLabel;
            this.isRequired = isRequired;
        }

        /**
         * Set tha value of the textValue field from the map with key=tag field.
         * @param values Map to get information from. If key=tag doesn't exist, set the value as ""
         *               and put into map key-value as tag-"" to make them equals.
         */
        void setTextValue(@NonNull Map<String, Object> values) {
            textValue.set((String) values.get(tag));
            updateEditedLabel();
        }

        void setLabel(String label) {
            if (isRequired) {
                label = label + requiredSign;
            }
            textValueLabelDefault = label;
            valueLabel.set(label);
        }

        /**
         * Set the edited sign, or remove it - if it was previously set.
         */
        void updateEditedLabel() {
            String label = textValueLabelDefault;
            if (updatedFields.contains(tag)) {
                label = label + editedSign;
            }
            valueLabel.set(label);
        }
    }
}
