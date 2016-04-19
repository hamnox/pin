package hamlah.pin;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import hamlah.pin.complice.Complice;
import hamlah.pin.complice.CompliceRemoteTask;
import rx.Observable;

public class CompliceListActivity extends AppCompatActivity {

    private static final String TAG = CompliceListActivity.class.getSimpleName();
    private static Pattern _pattern = Pattern.compile("^[^) ]]*\\)?\\s*(.*)$");

    @Bind(R.id.complice_list)
    RecyclerView _recyclerView;

    @Nullable
    private List<CompliceRemoteTask> _compliceList;

    private CompliceListAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complice_list);
        ButterKnife.bind(this);

        _recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        _recyclerView.setLayoutManager(layoutManager);
        _adapter = new CompliceListAdapter();
        _recyclerView.setAdapter(_adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.wrap(Complice.get().getActionList()).subscribe(list -> {
            _compliceList = Observable.from(list)
                    .filter(x -> !x.isDone() && !x.isNevermind())
                    .toList().toBlocking().first();

            _adapter.notifyDataSetChanged();
            Log.i(TAG, "got result");
        });
    }

    class ListItemHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.text)
        TextView _textView;

        @Bind(R.id.code)
        TextView _codeView;

        @Bind(R.id.mainview)
        View _mainView;

        public ListItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private class CompliceListAdapter extends RecyclerView.Adapter<ListItemHolder> {
        @Override
        public ListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.complice_list_item, parent, false);
            return new ListItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ListItemHolder holder, int position) {
            if (_compliceList == null) {
                return;
            }
            CompliceRemoteTask item = _compliceList.get(position);

            holder._mainView.setBackgroundColor(item.getDarkSquashedColor() | 0xff000000);

            Matcher matcher = _pattern.matcher(item.getLabel());
            if (!matcher.matches()) {
                holder._textView.setText(item.getLabel());
            } else {
                holder._textView.setText(matcher.group(1));
            }
            if (item.getGoalCode().equals("x")) {
                holder._codeView.setText("&");
            } else {
                holder._codeView.setText(item.getGoalCode());
            }
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(item.getMidSquashedColor() | 0xff000000);
            holder._codeView.setBackground(drawable);
        }

        @Override
        public int getItemCount() {
            if (_compliceList != null) {
                return _compliceList.size();
            } else {
                return 0;
            }
        }
    }
}
