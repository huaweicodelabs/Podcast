/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.podcast.java.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.main.view.DetailsActivity;
import com.huawei.podcast.java.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements Filterable {
    ArrayList<String> searchList;
    ArrayList<String> categoryList;
    Context context;

    public SearchAdapter(ArrayList<String> categoryFilterList, Context context) {
        this.categoryList = categoryFilterList;
        this.context = context;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_search, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final String category = categoryList.get(position);
        holder.txt_search.setText(category);
        holder.cv_search.setOnClickListener(
                view -> {
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra(context.getString(R.string.topic), category);
                    context.startActivity(intent);
                });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    @Override
    public Filter getFilter() {
        return topicFilter;
    }

    private final Filter topicFilter =
            new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    List<String> filterList = new ArrayList<>();
                    if (constraint == null || constraint.length() == Constants.ZERO) {
                        filterList.addAll(searchList);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (String item : searchList) {
                            if (item.toLowerCase().contains(filterPattern)) {
                                filterList.add(item);
                            }
                        }
                    }
                    FilterResults results = new FilterResults();
                    results.values = filterList;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    categoryList.clear();
                    categoryList.addAll((List) results.values);
                    notifyDataSetChanged();
                }
            };

    public void setList(ArrayList<String> categoryList) {
        this.searchList = new ArrayList<>(categoryList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cv_search;
        public TextView txt_search;

        public ViewHolder(View itemView) {
            super(itemView);
            this.cv_search = itemView.findViewById(R.id.cv_search);
            this.txt_search = itemView.findViewById(R.id.txt_search);
        }
    }
}
