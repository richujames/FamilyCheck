package org.familycheck.senior;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
public class DashboardFragment extends Fragment {
    private Db db;
    private LinearLayout body;
    private int tab;
    private String query="",filter="All checks";
    private boolean oldest;
    private CardAdapter adapter;
    private final ArrayList<HashMap<String,String>> visible=new ArrayList<>();
    public static DashboardFragment create(int tab) {
        DashboardFragment f=new DashboardFragment();
        Bundle b=new Bundle();
        b.putInt("tab",tab);
        f.setArguments(b);
        return f;
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        db=new Db(getActivity());
        tab=getArguments().getInt("tab");
        if(saved!=null) {
            query=saved.getString("query","");
            filter=saved.getString("filter","All checks");
            oldest=saved.getBoolean("oldest");
        }
    }
    @Override public View onCreateView(LayoutInflater i,ViewGroup p,Bundle saved) {
        body=Ui.column(getActivity());
        int pad=Ui.dp(getActivity(),20);
        body.setPadding(pad,0,pad,pad);
        if(tab==0) {
            ScrollView scroll=new ScrollView(getActivity());
            scroll.addView(body);
            home();
            return scroll;
        }
        if(tab==1)checks();
        else if(tab==2)people();
        else learn();
        return body;
    }
    private void open(Class<?> c) {
        startActivity(new Intent(getActivity(),c));
    }
    private void home() {
        body.removeAllViews();
        Ui.text(body,"PAUSE  /  CHECK  /  CONNECT",12,true).setTextColor(Ui.TEAL);
        Ui.text(body,"A little help.\nMore peace of mind.",30,true);
        Ui.text(body,"Unsure about a message or call? You don't have to decide alone.",17,false);
        LinearLayout hero=Ui.card(body);
        hero.setBackground(Ui.background(getActivity(),0xFF172E3C));
        Ui.text(hero,"Let's check it together",22,true).setTextColor(android.graphics.Color.WHITE);
        Ui.text(hero,"Notice warning signs, then ask someone you trust.",16,false).setTextColor(0xFFDCE8E9);
        Ui.button(hero,"+  Start a check",true,()->open(CheckActivity.class));
        int count=0;
        for(HashMap<String,String> r:db.rows("cases"))if("Still unsure".equals(r.get("status")))count++;
        LinearLayout progress=Ui.card(body);
        Ui.text(progress,count+" awaiting a follow-up",21,true);
        Ui.button(progress,"Review your checks",false,()->((MainActivity)getActivity()).select(1));
        ArrayList<HashMap<String,String>> people=db.rows("contacts");
        if(!people.isEmpty()) {
            HashMap<String,String> person=people.get(0);
            Ui.text(body,"YOUR TRUSTED PERSON",12,true);
            Ui.button(body,"Call "+person.get("name"),false,()->dial(person));
        }
        else Ui.button(body,"+  Add someone you trust",false,()->new ContactDialog().show(getFragmentManager(),"contact"));
        Ui.text(body,"ONE SMALL REMINDER",12,true);
        Ui.text(body,"Pressure to act now is a reason to pause. Take a moment and call a known number.",17,false);
        Ui.button(body,"Try a 5-question practice",false,()->open(QuizActivity.class));
    }
    private ListView list() {
        ListView l=new ListView(getActivity());
        l.setDivider(new android.graphics.drawable.ColorDrawable(0));
        l.setDividerHeight(Ui.dp(getActivity(),10));
        body.addView(l,new LinearLayout.LayoutParams(-1,0,1));
        adapter=new CardAdapter(getActivity(),visible);
        l.setAdapter(adapter);
        TextView empty=Ui.text(body,tab==1?"No matching checks. Start one or change your search.":"Add a family member or friend to get started.",18,false);
        l.setEmptyView(empty);
        registerForContextMenu(l);
        return l;
    }
    private void search(String hint) {
        EditText s=new EditText(getActivity());
        s.setHint(hint);
        s.setTextSize(Ui.size(getActivity(),17));
        s.setSingleLine();
        s.setText(query);
        body.addView(s);
        s.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int c,int f) {
            }
            public void afterTextChanged(Editable e) {
            }
            public void onTextChanged(CharSequence s,int a,int b,int c) {
                query=s.toString();
                refresh();
            }
        }
        );
    }
    private void checks() {
        Ui.text(body,"Your checks",28,true);
        search("Search a message or note");
        String[] filters= {
            "All checks","Still unsure","Reported as suspicious","Verified independently"
        }
        ;
        Spinner spinner=new Spinner(getActivity());
        spinner.setAdapter(new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_dropdown_item,filters));
        spinner.setMinimumHeight(Ui.dp(getActivity(),48));
        body.addView(spinner);
        for(int n=0;n<filters.length;n++)if(filters[n].equals(filter))spinner.setSelection(n);
        LinearLayout actions=new LinearLayout(getActivity());
        body.addView(actions);
        Button add=Ui.button(actions,"+ New",true,()->open(CheckActivity.class));
        add.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        Button sort=Ui.button(actions,oldest?"Oldest first":"Newest first",false,()-> {
        }
        );
        sort.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        sort.setOnClickListener(v-> {
            oldest=!oldest;
            sort.setText(oldest?"Oldest first":"Newest first");
            refresh();
        }
        );
        ListView l=list();
        l.setOnItemClickListener((p,v,pos,id)->openCase(visible.get(pos)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(AdapterView<?> p) {
            }
            public void onItemSelected(AdapterView<?> p,View v,int pos,long id) {
                filter=filters[pos];
                refresh();
            }
        }
        );
        refresh();
    }
    private void people() {
        Ui.text(body,"Your circle",28,true);
        Ui.text(body,"Favourite people appear first. Tap for calling, messaging or editing.",16,false);
        search("Find a trusted person");
        Ui.button(body,"+ Add a person",true,()->new ContactDialog().show(getFragmentManager(),"contact"));
        ListView l=list();
        l.setOnItemClickListener((p,v,pos,id)->personMenu(visible.get(pos)));
        refresh();
    }
    private void personMenu(HashMap<String,String> r) {
        new AlertDialog.Builder(getActivity()).setTitle(r.get("name")).setItems(new String[] {
            "Open dialer","Write an SMS","Edit person","Remove person"
        }
        ,(d,w)-> {
            if(w==0)dial(r);
            else if(w==1)Ui.launch(getActivity(),new Intent(Intent.ACTION_SENDTO,Uri.fromParts("smsto",r.get("phone"),null)).putExtra("sms_body","Could you help me check a message or call before I act?"));
            else if(w==2)ContactDialog.edit(r).show(getFragmentManager(),"contact");
            else delete(r);
        }
        ).show();
    }
    private void dial(HashMap<String,String> r) {
        Ui.launch(getActivity(),new Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",r.get("phone"),null)));
    }
    private void openCase(HashMap<String,String> r) {
        startActivity(new Intent(getActivity(),DetailActivity.class).putExtra("id",Long.parseLong(r.get("_id"))));
    }
    private void learn() {
        Ui.text(body,"Build your confidence",28,true);
        Ui.text(body,"Short lessons. Practical next steps.",17,false);
        Ui.button(body,"Practice: spot the warning signs",true,()->open(QuizActivity.class));
        GridView grid=new GridView(getActivity());
        grid.setNumColumns(getResources().getConfiguration().screenWidthDp>=600?2:1);
        grid.setVerticalSpacing(Ui.dp(getActivity(),10));
        grid.setHorizontalSpacing(Ui.dp(getActivity(),10));
        ArrayList<HashMap<String,String>> cards=new ArrayList<>();
        for(int n=0;n<Lessons.TITLES.length;n++)cards.add(CardAdapter.card("0"+(n+1),Lessons.TITLES[n],Lessons.SUBTITLES[n]));
        grid.setAdapter(new CardAdapter(getActivity(),cards));
        body.addView(grid,new LinearLayout.LayoutParams(-1,0,1));
        grid.setOnItemClickListener((p,v,pos,id)->new AlertDialog.Builder(getActivity()).setTitle(Lessons.TITLES[pos]).setMessage(Lessons.TEXT[pos]).setPositiveButton("Got it",null).setNeutralButton("Read more",(d,w)->Ui.launch(getActivity(),new Intent(Intent.ACTION_VIEW,Uri.parse("https://consumer.ftc.gov/scams")))).show());
    }
    public void refresh() {
        if(body==null)return;
        if(tab==0) {
            home();
            return;
        }
        if(adapter==null)return;
        visible.clear();
        ArrayList<HashMap<String,String>> rows=db.rows(tab==1?"cases":"contacts");
        if(tab==1&&oldest)Collections.reverse(rows);
        for(HashMap<String,String> r:rows) {
            String haystack=tab==1?r.get("message")+" "+r.get("note"):r.get("name")+" "+r.get("relationship");
            if(!haystack.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;
            if(tab==1) {
                if(!filter.equals("All checks")&&!filter.equals(r.get("status")))continue;
                r.put("title",r.get("message"));
                r.put("subtitle",r.get("status")+"\n"+r.get("source")+" · "+android.text.format.DateFormat.format("dd MMM",Long.parseLong(r.get("created"))));
                r.put("badge","?");
            }
            else {
                r.put("title",r.get("name"));
                r.put("subtitle",r.get("relationship")+" · "+r.get("phone"));
                r.put("badge","1".equals(r.get("favorite"))?"★":r.get("name").substring(0,1).toUpperCase(Locale.ROOT));
            }
            visible.add(r);
        }
        adapter.notifyDataSetChanged();
    }
    private void delete(HashMap<String,String> r) {
        new AlertDialog.Builder(getActivity()).setTitle(tab==1?"Delete this check?":"Remove this person?").setMessage("This cannot be undone.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)-> {
            db.delete(tab==1?"cases":"contacts",Long.parseLong(r.get("_id")));
            refresh();
        }
        ).show();
    }
    @Override public void onCreateContextMenu(ContextMenu m,View v,ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(m,v,info);
        m.setHeaderTitle(tab==1?"Saved check":"Trusted person");
        m.add(0,51,0,tab==1?"Open":"Edit");
        m.add(0,52,1,"Delete");
    }
    @Override public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId()!=51&&item.getItemId()!=52)return false;
        HashMap<String,String> r=visible.get(((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position);
        if(item.getItemId()==52)delete(r);
        else if(tab==1)openCase(r);
        else ContactDialog.edit(r).show(getFragmentManager(),"contact");
        return true;
    }
    @Override public void onResume() {
        super.onResume();
        refresh();
    }
    @Override public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString("query",query);
        b.putString("filter",filter);
        b.putBoolean("oldest",oldest);
    }
    @Override public void onDestroyView() {
        body=null;
        adapter=null;
        super.onDestroyView();
    }
    @Override public void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
