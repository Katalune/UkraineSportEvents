package ua.com.sportevent.sportevent;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.com.sportevent.sportevent.databinding.ListItemEventLargeBinding;
import ua.com.sportevent.sportevent.databinding.ListItemEventMediumBinding;
import ua.com.sportevent.sportevent.databinding.ListItemEventSmallBinding;
import ua.com.sportevent.sportevent.helpers.PreferenceHelper;
import ua.com.sportevent.sportevent.jsonModels.ServerEvent;
import ua.com.sportevent.sportevent.viewModels.EventInfo;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    final OnClickHandler mClickHandler;
    int mLayoutType;
    List<EventInfo> mEventInfos;
    private Locale mLocale;

    /**
     * Update source data of the adapter.
     * @param serverEvent event to create new data resources.
     */
    public void updateData(ServerEvent serverEvent) {
        if (serverEvent == null) {
            mEventInfos = null;
            notifyDataSetChanged();
        } else {
            EventInfo data = new EventInfo(serverEvent, mLocale);
            if (mEventInfos == null) {
                mEventInfos = new ArrayList<>();
            }
            mEventInfos.add(data);
            // It will always be more efficient to use the more specific change events,
            // as in this case we update last element.
            notifyItemInserted(mEventInfos.size() - 1);
        }
    }

    public void setLayoutType(int layoutType) {
        mLayoutType = layoutType;
        notifyDataSetChanged();
    }

    // Interface to pass handle of list item click to the Parent Activity.
    public interface OnClickHandler {
        void onEventItemClick();
    }

    // Provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Each data item is event layout.
        ViewDataBinding bindingEvent;

        public ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            // Switch binding
            bindingEvent = binding;
            bindingEvent.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Position of the current event in the data array as tag added to the view
            // in the onBindViewHolder() method.
            int position = (int) view.getTag();
            PreferenceHelper.setDetailsEventId(view.getContext(), mEventInfos.get(position).mEventId);
            mClickHandler.onEventItemClick();
        }
    }

    /**
     * Adapter for event list.
     * @param ch Object to handle event item click (implements interface).
     */
    public EventAdapter(OnClickHandler ch, int layout, Locale locale) {
        mLocale = locale;
        mClickHandler = ch;
        mLayoutType = layout;
    }

    // Create new views (invoked by the layout manager).
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        // Switch layout
        return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                viewType, parent, false));
    }

    /*
      This is where we fill-in the views with the contents of the data source (invoked by the
      layout manager).
    */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        holder.bindingEvent.getRoot().setTag(adapterPosition);
        // - get element from your dataset at this position
        EventInfo info = mEventInfos.get(adapterPosition);
        // Switch binding
        switch (mLayoutType) {
            case R.layout.list_item_event_large:
                ((ListItemEventLargeBinding)holder.bindingEvent).setEvent(info);
                break;
            case R.layout.list_item_event_medium:
                ((ListItemEventMediumBinding)holder.bindingEvent).setEvent(info);
                break;
            case R.layout.list_item_event_small:
                ((ListItemEventSmallBinding)holder.bindingEvent).setEvent(info);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (null == mEventInfos) return 0;
        return mEventInfos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mLayoutType;
    }
}