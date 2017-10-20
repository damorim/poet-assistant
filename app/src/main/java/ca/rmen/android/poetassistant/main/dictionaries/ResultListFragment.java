/*
 * Copyright (c) 2016-2017 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main.dictionaries;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

import ca.rmen.android.poetassistant.Constants;
import ca.rmen.android.poetassistant.Favorite;
import ca.rmen.android.poetassistant.R;
import ca.rmen.android.poetassistant.databinding.BindingCallbackAdapter;
import ca.rmen.android.poetassistant.databinding.FragmentResultListBinding;
import ca.rmen.android.poetassistant.main.AppBarLayoutHelper;
import ca.rmen.android.poetassistant.main.Tab;


public class ResultListFragment<T> extends Fragment {
    private static final String TAG = Constants.TAG + ResultListFragment.class.getSimpleName();
    public static final String EXTRA_TAB = "tab";
    static final String EXTRA_QUERY = "query";
    private static final String EXTRA_FILTER = "filter";
    private FragmentResultListBinding mBinding;
    private ResultListViewModel<T> mViewModel;
    private ResultListHeaderViewModel mHeaderViewModel;

    private Tab mTab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTab = (Tab) getArguments().getSerializable(EXTRA_TAB);
        Log.v(TAG, mTab + " onCreateView");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_result_list, container, false);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mBinding.recyclerView.setHasFixedSize(true);
        //noinspection unchecked
        mViewModel = (ResultListViewModel<T>) ResultListFactory.createViewModel(mTab, this);
        mBinding.setViewModel(mViewModel);
        mViewModel.layout.addOnPropertyChangedCallback(mLayoutSettingChanged);
        mViewModel.showHeader.addOnPropertyChangedCallback(mShowHeaderChanged);
        mViewModel.isDataAvailable.addOnPropertyChangedCallback(mDataAvailableChanged);
        mHeaderViewModel = ViewModelProviders.of(this).get(ResultListHeaderViewModel.class);
        mHeaderViewModel.filter.addOnPropertyChangedCallback(mFilterChanged);
        Fragment headerFragment = getChildFragmentManager().findFragmentById(R.id.result_list_header);
        if (headerFragment == null) {
            headerFragment = ResultListHeaderFragment.newInstance(mTab);
            getChildFragmentManager().beginTransaction().replace(R.id.result_list_header, headerFragment).commit();
        }
        mViewModel.favoritesLiveData.observe(this, mFavoritesObserver);
        mViewModel.resultListDataLiveData.observe(this, data -> mViewModel.setData(data));
        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, mTab + ": onActivityCreated() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        //noinspection unchecked
        ResultListAdapter<T> adapter = (ResultListAdapter<T>) ResultListFactory.createAdapter(getActivity(), mTab);
        mViewModel.setAdapter(adapter);
        mBinding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        Log.v(TAG, mTab + " onStart");
        super.onStart();
        queryFromArguments();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDestroyView() {
        Log.v(TAG, mTab + " onDestroyView");
        mViewModel.layout.removeOnPropertyChangedCallback(mLayoutSettingChanged);
        mViewModel.layout.removeOnPropertyChangedCallback(mShowHeaderChanged);
        mViewModel.isDataAvailable.removeOnPropertyChangedCallback(mDataAvailableChanged);
        mHeaderViewModel.filter.removeOnPropertyChangedCallback(mFilterChanged);
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            mViewModel.share(mTab, mHeaderViewModel.query.get(), mHeaderViewModel.filter.get());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share).setEnabled(mViewModel.isDataAvailable.get());
    }

    private void queryFromArguments() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_QUERY)) {
            mViewModel.setQueryParams(new ResultListViewModel.QueryParams(
                    args.getString(EXTRA_QUERY),
                    args.getString(EXTRA_FILTER)
            ));
        }
    }

    public void query(String query) {
        Log.d(TAG, mTab + ": query() called with: " + "query = [" + query + "]");
        mHeaderViewModel.filter.set(null);
        mViewModel.setQueryParams(new ResultListViewModel.QueryParams(query, null));
        getActivity().invalidateOptionsMenu();
    }

    private final BindingCallbackAdapter mDataAvailableChanged =
            new BindingCallbackAdapter(() -> {
                if (getUserVisibleHint()) {
                    if (mViewModel.isDataAvailable.get()) {
                        AppBarLayoutHelper.enableAutoHide(getActivity());
                    } else {
                        AppBarLayoutHelper.disableAutoHide(getActivity());
                    }
                }
                getActivity().invalidateOptionsMenu();

                mHeaderViewModel.query.set(mViewModel.getUsedQueryWord());
                // Hide the keyboard
                mBinding.recyclerView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mBinding.recyclerView.getWindowToken(), 0);
            });

    private final BindingCallbackAdapter mShowHeaderChanged =
            new BindingCallbackAdapter(() -> mHeaderViewModel.showHeader.set(mViewModel.showHeader.get()));

    private final BindingCallbackAdapter mFilterChanged = new BindingCallbackAdapter(this::reload);

    private final BindingCallbackAdapter mLayoutSettingChanged = new BindingCallbackAdapter(this::reload);

    private final Observer<List<Favorite>> mFavoritesObserver = favorites -> reload();

    private void reload() {
        mViewModel.setQueryParams(new ResultListViewModel.QueryParams(mHeaderViewModel.query.get(),
                mHeaderViewModel.filter.get()));
    }
}
