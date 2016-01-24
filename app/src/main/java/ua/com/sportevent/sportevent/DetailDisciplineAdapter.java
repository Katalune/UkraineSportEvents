package ua.com.sportevent.sportevent;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import ua.com.sportevent.sportevent.databinding.ListItemDetailContentBinding;
import ua.com.sportevent.sportevent.databinding.ListItemDetailDisciplinePackageBinding;
import ua.com.sportevent.sportevent.helpers.BuyingInformation;
import ua.com.sportevent.sportevent.helpers.Utility;
import ua.com.sportevent.sportevent.jsonModels.DisciplinePackage;
import ua.com.sportevent.sportevent.jsonModels.ExpandedPackage;
import ua.com.sportevent.sportevent.jsonModels.ServerEvent;
import ua.com.sportevent.sportevent.jsonModels.SportDiscipline;
import ua.com.sportevent.sportevent.viewModels.EventInfo;
import ua.com.sportevent.sportevent.viewModels.EventPackage;

/**
 * Handle binding discipline with childes information to the view.
 */
public class DetailDisciplineAdapter extends RecyclerView.Adapter<DetailDisciplineAdapter.ViewHolder> {
    ServerEvent mServerEvent;
    private final int MAIN_CONTENT = 0;
    private final int DISCIPLINE = 1;
    private final int TEXT = 2;
    private Locale mLocale;

    public DetailDisciplineAdapter(Locale locale) {
        mLocale = locale;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        switch (viewType) {
            case MAIN_CONTENT:
                return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.list_item_detail_content, parent, false));
            case DISCIPLINE:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_detail_discipline, parent, false);
                return new ViewHolder(v);
            case TEXT:
            default:
                TextView textView = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_detail_reg_textview, parent, false);
                return new ViewHolder(textView);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        switch (getItemViewType(adapterPosition)) {
            case MAIN_CONTENT:
                holder.mContentBinding.setEventItem(new EventInfo(mServerEvent, mLocale));
                break;
            case DISCIPLINE:
                // Clean up from the previously added (maybe foreign) views.
                holder.removePackages();
                // 2nd position is the 0 discipline
                SportDiscipline discipline = mServerEvent.disciplines[adapterPosition - 2];
                ArrayList<ExpandedPackage> expanded = new ArrayList<>(1);
                processPackages(new ArrayList<>(Arrays.asList(discipline.tariffs)), expanded);
                ExpandedPackage[] expandedArray = new ExpandedPackage[expanded.size()];
                expanded.toArray(expandedArray);
                holder.mDisciplineNameView.setText(discipline.name);
                holder.mDisciplinePackages = expandedArray;
                holder.mDisciplineId = discipline.id;
                holder.mDisciplineName = discipline.name;
                holder.mRootView.setTag(adapterPosition);
        }
    }

    private void processPackages(@NonNull ArrayList<DisciplinePackage> disciplinePackages, ArrayList<ExpandedPackage> expandedPackages) {
        for (int i = 0; i < disciplinePackages.size(); i++) {
            if (!disciplinePackages.get(i).isValid(Utility.getServerDate())) {
                disciplinePackages.remove(i);
                i--;
            }
        }
        expandedPackages.add(new ExpandedPackage(disciplinePackages.remove(0)));

        for (int i = 0; i < disciplinePackages.size(); i++) {
            boolean found = false;
            for (int j = 0, len = expandedPackages.size(); j < len; j++) {
                if (expandedPackages.get(j).merge(disciplinePackages.get(i))) {
                    disciplinePackages.remove(i);
                    i--;
                    found = true;
                    break;
                }
            }
            if (!found) {
                expandedPackages.add(new ExpandedPackage(disciplinePackages.get(i)));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return MAIN_CONTENT;
            case 1:
                return TEXT;
            default:
                return DISCIPLINE;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (mServerEvent == null) return 0;
        return mServerEvent.disciplines.length + 2;
    }

    /**
     * Update source data of the adapter.
     * @param data new data resources.
     */
    public void updateData(ServerEvent data) {
            mServerEvent = data;
            notifyDataSetChanged();
   }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Each data item is discipline layout.
        ListItemDetailContentBinding mContentBinding;
        ViewGroup mRootView;
        TextView mDisciplineNameView;
        ExpandedPackage[] mDisciplinePackages;
        String mDisciplineId;
        String mDisciplineName;

        public ViewHolder(View itemView) {
            super(itemView);
            mRootView = (LinearLayout) itemView.findViewById(R.id.detail_discipline_root);
            mDisciplineNameView = (TextView) itemView.findViewById(R.id.detail_discipline_name);

            mDisciplineNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchPackages();
                }
            });
        }

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mContentBinding = (ListItemDetailContentBinding) binding;
        }

        public ViewHolder(TextView textView) {
            super(textView);
        }

        void switchPackages() {
            if (mRootView.getChildCount() > 1) {
                notifyItemChanged((Integer) mRootView.getTag());
            } else {
                addPackages();
            }
        }

        void addPackages() {
            mDisciplineNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
            for (ExpandedPackage expPackage : mDisciplinePackages) {
                ListItemDetailDisciplinePackageBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mRootView.getContext()),
                        R.layout.list_item_detail_discipline_package, mRootView, false);
                binding.setDPackage(new EventPackage(expPackage, mRootView.getContext()));

                BuyingInformation info = new BuyingInformation(mServerEvent.id, mDisciplineId, expPackage.id, mDisciplineName, expPackage.tariff);
                binding.disciplineButtonBuy.setTag(R.id.buying_information, info);

                mRootView.addView(binding.getRoot());
            }
        }

        void removePackages() {
            mDisciplineNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
            while (mRootView.getChildCount() > 1) {
                mRootView.removeViewAt(1);
            }
        }
    }
}
