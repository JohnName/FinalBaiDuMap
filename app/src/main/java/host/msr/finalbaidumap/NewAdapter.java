package host.msr.finalbaidumap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ymmmsick on 2016/4/26.
 */
public class NewAdapter extends BaseAdapter {

    Context context;
    List<String> lists;
    LayoutInflater inflater;

    public NewAdapter(Context context, List<String> lists) {
        this.context = context;
        this.lists = lists;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item, null);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.item_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(lists.get(position));
        return convertView;
    }

    class ViewHolder {
        TextView textView;
    }
}
