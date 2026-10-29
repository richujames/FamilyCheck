package org.familycheck.senior;
import android.app.*;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;
public class BaseActivity extends Activity {
    protected LinearLayout content;
    protected Db db;
    protected void setup(String title) {
        db=new Db(this);
        LinearLayout root=Ui.column(this);
        Ui.insets(root);
        Toolbar bar=new Toolbar(this);
        bar.setTitle(title);
        bar.setNavigationIcon(android.R.drawable.ic_menu_revert);
        bar.setNavigationContentDescription("Back");
        bar.setNavigationOnClickListener(v->finish());
        root.addView(bar);
        ScrollView scroll=new ScrollView(this);
        content=Ui.column(this);
        int p=Ui.dp(this,20);
        content.setPadding(p,p,p,p);
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    protected EditText input(String label,int id,int limit) {
        Ui.text(content,label,16,true);
        EditText e=new EditText(this);
        e.setId(id);
        e.setHint(label);
        e.setTextSize(Ui.size(this,18));
        e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        e.setFilters(new android.text.InputFilter[] {
            new android.text.InputFilter.LengthFilter(limit)
        }
        );
        content.addView(e);
        return e;
    }
    @Override protected void onDestroy() {
        if(db!=null)db.close();
        super.onDestroy();
    }
}
