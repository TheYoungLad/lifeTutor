package com.rcs.nchumanity.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * 由于我们不在清除，内部使用什么样的bean来作为封装的数据，所以我们只能通过泛型机制进行bean测接收
 * <p>
 * 该类是listview的通用适配器，实现是的是通用的效果实现，对于部分特别的功能，如果考虑listview的item错乱问题
 * 可以使用原始的最基本的开发实现方式。
 *
 * @param <T>
 */
public abstract class ListViewCommonsAdapter<T> extends BaseAdapter {
    private ArrayList<T> mData;
    private int mLayoutRes;

    //布局id
    public ListViewCommonsAdapter() {
    }

    public ListViewCommonsAdapter(ArrayList<T> mData, int mLayoutRes) {
        this.mData = mData;
        this.mLayoutRes = mLayoutRes;
    }

    public void setmData(ArrayList<T> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }

    @Override
    public T getItem(int position) {
        if (mData.size() == 0) {
            return null;
        }
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.bind(parent.getContext(), convertView, parent, mLayoutRes, position);
        bindView(holder, getItem(position));
        return holder.getItemView();
    }


    public abstract void bindView(ViewHolder holder, T obj);

    //添加一个元素
    public void add(T data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(data);
        notifyDataSetChanged();
    }

    //往特定位置,添加一个元素
    public void add(int position, T data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.add(position, data);
        notifyDataSetChanged();
    }

    public void remove(T data) {
        if (mData != null) {
            mData.remove(data);
        }
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (mData != null) {
            mData.remove(position);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
        notifyDataSetChanged();
    }


    public static class ViewHolder {
        private SparseArray<View> mViews; //存储ListView 的 item中的
        private View item; //存放convertView
        private int position; //游标
        private Context context;//Context上下文View

        private ViewHolder(Context context, ViewGroup parent, int layoutRes) {
            mViews = new SparseArray<>();
            this.context = context;
            View convertView = LayoutInflater.from(context).inflate(layoutRes, parent, false);
            convertView.setTag(this);
            item = convertView;
        }

        public static ViewHolder bind(Context context, View convertView, ViewGroup parent, int layoutRes, int position) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(context, parent, layoutRes);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.item = convertView;
            }
            holder.position = position;
            return holder;
        }


        @SuppressWarnings("unchecked")
        public <T extends View> T getView(int id) {
            T t = (T) mViews.get(id);
            if (t == null) {
                t = (T) item.findViewById(id);
                mViews.put(id, t);
            }
            return t;
        }

        public View getItemView() {
            return item;
        }

        public int getItemPosition() {
            return position;
        }


        public ViewHolder setText(int id, CharSequence text) {
            View view = getView(id);
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }


        public ViewHolder setImageResource(int id, @DrawableRes int drawableRes) {
            View view = getView(id);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawableRes);
            } else {
                view.setBackgroundResource(drawableRes);
            }
            return this;
        }


        public ViewHolder setAsyncImageResource(int id, String drawableRes) {
            View view = getView(id);
            if (view instanceof ImageView) {
                Glide.with(context).load(drawableRes).into((ImageView) view);
            }
            return this;
        }


        public ViewHolder setOnClickListener(int id, View.OnClickListener listener) {
            getView(id).setOnClickListener(listener);
            return this;
        }

        public ViewHolder setVisibility(int id, int visible) {
            getView(id).setVisibility(visible);
            return this;
        }

        public ViewHolder setTag(int id, Object obj) {
            getView(id).setTag(obj);
            return this;
        }

        public void setOnCheckChangeListener(int id, CompoundButton.OnCheckedChangeListener changeListener) {
            View view = getView(id);
            if (view instanceof CheckBox) {
                ((CheckBox) view).setOnCheckedChangeListener(changeListener);
            }

        }

        /**
         * 设置 上面的drawable
         * @param id
         * @param drawableId
         */
        public void setDrawableTop(int id, int drawableId) {
            View view=getView(id);
            if(view instanceof TextView){
                 Drawable res=view.getContext().getResources().getDrawable(drawableId);
                res.setBounds(0, 0, res.getMinimumWidth(), res.getMinimumHeight());
                ((TextView) view).setCompoundDrawables(null,res,null,null);
            }
        }
    }

}