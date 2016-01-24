package ua.com.sportevent.sportevent.helpers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcel to pass data about event, discipline and package to buy.
 */
public class BuyingInformation implements Parcelable {
    private String mEventId;
    private String mDisciplineId;
    private String mPackageId;
    private String mDisciplineName;
    private String mPackageName;

    public BuyingInformation(String eventId, String disciplineId, String packageId, String disciplineName, String packageName) {
        mEventId = eventId;
        mDisciplineId = disciplineId;
        mPackageId = packageId;
        mDisciplineName = disciplineName;
        mPackageName = packageName;
    }

    public String getEventId() {
        return mEventId;
    }

    public String getDisciplineId() {
        return mDisciplineId;
    }

    public String getPackageId() {
        return mPackageId;
    }

    public String getDisciplineName() {
        return mDisciplineName;
    }

    public String getPackageName() {
        return mPackageName;
    }

    protected BuyingInformation(Parcel in) {
        mEventId = in.readString();
        mDisciplineId = in.readString();
        mPackageId = in.readString();
        mDisciplineName = in.readString();
        mPackageName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mEventId);
        dest.writeString(mDisciplineId);
        dest.writeString(mPackageId);
        dest.writeString(mDisciplineName);
        dest.writeString(mPackageName);
    }

    public static final Creator<BuyingInformation> CREATOR = new Creator<BuyingInformation>() {
        @Override
        public BuyingInformation createFromParcel(Parcel in) {
            return new BuyingInformation(in);
        }

        @Override
        public BuyingInformation[] newArray(int size) {
            return new BuyingInformation[size];
        }
    };
}
