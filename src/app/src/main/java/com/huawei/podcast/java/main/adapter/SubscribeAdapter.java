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
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.podcast.R;
import com.huawei.podcast.java.data.model.SubscribeModel;
import com.huawei.podcast.databinding.ItemSubscriptionListBinding;
import com.huawei.podcast.java.interfaces.SubscribeClickListener;

import java.util.List;

public class SubscribeAdapter extends RecyclerView.Adapter<SubscribeAdapter.ViewHolder> {
    private final List<SubscribeModel> subscribeModels;
    Context context;

    public SubscribeAdapter(List<SubscribeModel> subscribeModels, Context context) {
        this.subscribeModels = subscribeModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemSubscriptionListBinding myListBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_subscription_list, parent, false);
        return new ViewHolder(myListBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubscribeModel subscribeModel = subscribeModels.get(position);
        holder.bind(context, subscribeModel);
    }

    @Override
    public int getItemCount() {
        return subscribeModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubscriptionListBinding itemSubscriptionListBinding;

        public ViewHolder(@NonNull ItemSubscriptionListBinding itemSubscriptionListBinding) {
            super(itemSubscriptionListBinding.getRoot());
            this.itemSubscriptionListBinding = itemSubscriptionListBinding;
        }

        public void bind(Context context, SubscribeModel subscribeModel) {
            this.itemSubscriptionListBinding.setType(false);
            this.itemSubscriptionListBinding.setSubscriptionModelListJava(subscribeModel);
            this.itemSubscriptionListBinding.setClickInterfaceJava((SubscribeClickListener) context);
            this.itemSubscriptionListBinding.setTopic(subscribeModel.getTopic());
            itemSubscriptionListBinding.executePendingBindings();
        }
    }
}
