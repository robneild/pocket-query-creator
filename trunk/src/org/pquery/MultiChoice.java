//package org.pquery;
//
//
///**
//* Copyright (C) 2010 Christian Meyer
//* This file is part of Drupal Editor.
//*
//* Drupal Editor is free software: you can redistribute it and/or modify
//* it under the terms of the GNU General Public License as published by
//* the Free Software Foundation, either version 2 of the License, or
//* (at your option) any later version.
//*
//* Drupal Editor is distributed in the hope that it will be useful,
//* but WITHOUT ANY WARRANTY; without even the implied warranty of
//* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//* GNU General Public License for more details.
//*
//* You should have received a copy of the GNU General Public License
//* along with Drupal Editor. If not, see <http://www.gnu.org/licenses/>.
//*/
////package ch.dissem.android.utils;
//
//import java.util.Collection;
//import java.util.LinkedHashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.Map.Entry;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.InputType;
//import android.text.TextWatcher;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//
///**
//* {@link BaseAdapter}-Implementation for the {@link MultiChoice} Dialog.
//* 
//* @author christian
//* @param <T>
//*            Type this Adapter contains. Should have some useful toString()
//*            implementation.
//*/
//class MultiChoiceListAdapter<T> extends BaseAdapter {
// private Context ctx;
//
// private Collection<T> options;
// private Collection<T> selection;
// private List<T> filteredOptions;
//
// public MultiChoiceListAdapter(Context context, Collection<T> options,
//     Collection<T> selection) {
//   this.ctx = context;
//
//   this.options = options;
//   this.selection = selection;
//
//   this.filteredOptions = new ArrayList<T>(options.size());
//   setFilter(null);
// }
//
// @Override
// public int getCount() {
//   return filteredOptions.size();
// }
//
// @Override
// public T getItem(int position) {
//   return filteredOptions.get(position);
// }
//
// @Override
// public long getItemId(int position) {
//   return position;
// }
//
// @Override
// public View getView(int position, View convertView, ViewGroup parent) {
//   ChoiceView view;
//   T item = getItem(position);
//   boolean selected = selection.contains(item);
//   if (convertView == null) {
//     view = new ChoiceView(ctx, item, selected);
//   } else {
//     view = (ChoiceView) convertView;
//     view.setItem(item, selected);
//   }
//   return view;
// }
//
// public void setFilter(String filter) {
//   if (filter != null)
//     filter = filter.toLowerCase();
//
//   filteredOptions.clear();
//   for (T item : selection)
//     filteredOptions.add(item);
//   for (T item : options)
//     if (!selection.contains(item)
//         && (filter == null || item.toString().toLowerCase()
//             .contains(filter)))
//       filteredOptions.add(item);
// }
//
// public class ChoiceView extends CheckBox implements OnCheckedChangeListener {
//   private T object;
//
//   public ChoiceView(Context context, T object, Boolean selected) {
//     super(context);
//     this.object = object;
//     setOnCheckedChangeListener(this);
//     setItem(object, selected);
//   }
//
//   @Override
//   public void onCheckedChanged(CompoundButton buttonView,
//       boolean isChecked) {
//     if (selection != null) {
//       if (isChecked && !selection.contains(object))
//         selection.add(object);
//       else if (!isChecked)
//         selection.remove(object);
//     }
//     notifyDataSetChanged();
//   }
//
//   public void setItem(T object, Boolean selected) {
//     this.object = object;
//     setChecked(selected);
//     setText(object.toString());
//   }
// }
//}
//
//
///**
//* A dialog that allows the user to select multiple entries.
//* 
//* @author christian
//* @param <T>
//*            Type for this dialog. Should have some useful toString()
//*            implementation.
//*/
//class MultiChoice<T> extends Dialog {
// private ListView listView;
//
// private Map<T, Boolean> optionsWithSelection;
// private Collection<T> options;
// private Collection<T> selection;
//
// public MultiChoice(Context context, Collection<T> options,
//     Collection<T> selection) {
//   super(context);
//   this.options = options;
//   this.selection = selection;
// }
//
// @Override
// protected void onCreate(Bundle savedInstanceState) {
//   super.onCreate(savedInstanceState);
//   Context ctx = getContext();
//   LinearLayout layout = new LinearLayout(ctx);
//   layout.setOrientation(LinearLayout.VERTICAL);
//
//   listView = new ListView(ctx);
//   final MultiChoiceListAdapter<T> adapter;
//   adapter = new MultiChoiceListAdapter<T>(ctx, options, selection);
//   listView.setAdapter(adapter);
//
//   if (options.size() > 10) {
//     EditText search = new EditText(ctx);
//     search.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
//     search.addTextChangedListener(new TextWatcher() {
//       @Override
//       public void onTextChanged(CharSequence s, int start,
//           int before, int count) {
//         adapter.setFilter(s.toString());
//         adapter.notifyDataSetChanged();
//       }
//
//       @Override
//       public void beforeTextChanged(CharSequence s, int start,
//           int count, int after) {
//       }
//
//       @Override
//       public void afterTextChanged(Editable s) {
//       }
//     });
//
//     layout.addView(search);
//   }
//
//   layout.addView(listView);
//   setContentView(layout);
// }
//
// public Map<T, Boolean> getOptionsMap() {
//   return optionsWithSelection;
// }
//
// public Set<T> getSelection() {
//   Set<T> result = new LinkedHashSet<T>();
//   for (Entry<T, Boolean> e : optionsWithSelection.entrySet())
//     if (Boolean.TRUE.equals(e.getValue()))
//       result.add(e.getKey());
//   return result;
// }
//}
