package org.familycheck.senior;
import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.*;
public class CardAdapter extends BaseAdapter {
    private final Context context;
    private final List<HashMap<String,String>> rows;
    public CardAdapter(Context c,List<HashMap<String,String>> r) {
        context=c;
        rows=r;
    }
    public int getCount() {
        return rows.size();
    }
    public HashMap<String,String> getItem(int p) {
        return rows.get(p);
    }
    public long getItemId(int p) {
        return p;
    }
    public View getView(int p,View recycled,ViewGroup parent) {
        View v=recycled==null?LayoutInflater.from(context).inflate(R.layout.list_card,parent,false):recycled;
        HashMap<String,String> r=getItem(p);
        ((TextView)v.findViewById(R.id.row_badge)).setText(r.get("badge"));
        TextView title=v.findViewById(R.id.row_title),sub=v.findViewById(R.id.row_subtitle);
        title.setText(r.get("title"));
        title.setTextSize(Ui.size(context,18));
        sub.setText(r.get("subtitle"));
        sub.setTextSize(Ui.size(context,14));
        return v;
    }
    public static HashMap<String,String> card(String badge,String title,String subtitle) {
        HashMap<String,String> r=new HashMap<>();
        r.put("badge",badge);
        r.put("title",title);
        r.put("subtitle",subtitle);
        return r;
    }
}
