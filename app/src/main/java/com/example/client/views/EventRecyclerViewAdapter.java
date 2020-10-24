package com.example.client.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.R;
import com.example.client.room.entity.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder> {

    private Context context;
    private ArrayList<Event> events;

    public EventRecyclerViewAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        /* Layout Manager call this function to retrieve new view */
        /* Step1: Initialize a layout/view */
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item = inflater.inflate(R.layout.event_view, parent, false);
        /* Step2: Create view holder and return it */
        return new EventViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        /* Layout Manager call this function to ask you to replace the content of the view */
        holder.event = events.get(position);
        holder.tv.setText(context.getString(events.get(position).eventId.getResId()));
        holder.updateTime(context);

        /* Apply specific timeline style(theme) to the timeline view */
        /* WTF is going on with these code?
         * Answer: This is very tricky to apply custom styles/attributes to views,
         *         We adopt a hacky solution here. First we change the
         *         theme of current application(since our style attribute is all custom(timelineColor, timelineIconBack...)
         *         it won't affect other usual views)
         *
         *         And then we create the new timeline related drawables.
         *         These drawable's style is defined during the creation, since we just update
         *         the theme and our custom attributes. The new drawable will create with the
         *         custom attributes we just set.
         */
        int styleId = events.get(position).eventId.getThemeId();
        context.getTheme().applyStyle(styleId, true);
        /* With the new theme, lets update the style of Event view */
        holder.imageView.setBackgroundResource(R.drawable.event_view_icon_background);
        /* Hack: this is a shame that Android provide the "state_last" for selector but never implement them */
        /*       We have to determine such condition by hard written logic below */
        if (position == events.size() - 1)
            holder.timeline.setBackgroundResource(R.drawable.event_view_timeline_last);
        else
            holder.timeline.setBackgroundResource(R.drawable.event_view_timeline);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        /* ViewHolder is the view for reach data item */
        private Event event;
        private TextView tv;
        private TextView tvTime;
        private FrameLayout timeline;
        private ImageView imageView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.textView);
            tvTime = itemView.findViewById(R.id.textViewTime);
            timeline = itemView.findViewById(R.id.timeline);
            imageView = itemView.findViewById(R.id.imageView);
        }

        private void setTimeText(String text) {
            tvTime.setText(text);
        }

        public boolean updateTime(Context context) {
            Date time = event.time;
            Calendar now = Calendar.getInstance();
            Calendar eventTime = Calendar.getInstance();
            eventTime.setTime(time);

            /* TODO: This method is not so robust since if the user change the time in the middle of the app, it will break :( */
            /* But let's be real who will change the system time of your phone :3. expect you go to other country */

            long millisecond_diff = now.getTimeInMillis() - eventTime.getTimeInMillis();
            long second = 1000;
            long minute = second * 60;
            long hour = minute * 60;
            long days = hour * 24;

            /* days after */
            if (millisecond_diff >= days) {
                setTimeText(android.text.format.DateFormat.getTimeFormat(context).format(time));
                return false;
            }

            /* hours after */
            if (millisecond_diff >= 2 * hour) {
                setTimeText(String.format(Locale.getDefault(), "%d hours ago", (millisecond_diff / hour)));
                return true;
            }
            if (millisecond_diff > hour) {
                setTimeText(String.format(Locale.getDefault(), "%d hour ago", (millisecond_diff / hour)));
                return true;
            }

            /* minutes after */
            if (millisecond_diff >= 2 * minute) {
                setTimeText(String.format(Locale.getDefault(), "%d minutes ago", (millisecond_diff / minute)));
                return true;
            }
            if (millisecond_diff >= minute) {
                setTimeText(String.format(Locale.getDefault(), "%d minute ago", (millisecond_diff / minute)));
                return true;
            }

            /* seconds after */
            if (millisecond_diff >= 10 * second) {
                setTimeText(String.format(Locale.getDefault(), "%d seconds ago", (millisecond_diff / second) / 10 * 10));
                return true;
            }

            /* about now */
            setTimeText("about now");
            return true;
        }

    }
}
