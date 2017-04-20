/*
 * Copyright (c) 2017 Carmen Alvarez
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

package ca.rmen.android.poetassistant.main;


import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Locale;

import ca.rmen.android.poetassistant.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ca.rmen.android.poetassistant.main.CustomViewActions.longTap;
import static ca.rmen.android.poetassistant.main.TestAppUtils.typePoem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.checkTitleStripCenterTitle;
import static ca.rmen.android.poetassistant.main.TestUiUtils.openMenuItem;
import static ca.rmen.android.poetassistant.main.TestUiUtils.swipeViewPagerLeft;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;

/**
 * Tested on:
 * - Huawei P9 Lite
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class PoemTest extends BaseTest {

    @Test
    public void exportAudioTest() {
        swipeViewPagerLeft(3);
        typePoem("Will export some text");
        File exportDir = new File(mActivityTestRule.getActivity().getFilesDir(), "export");
        File poemFile = new File(exportDir, "poem.wav");
        assertFalse(poemFile.exists());
        openMenuItem(R.string.share_poem_audio);
        assertTrue(poemFile.exists());
    }

    @Test
    public void lookupFromPoemTest() {
        swipeViewPagerLeft(3);
        String poemText = "Here is a poem";
        typePoem(poemText);

        // Look up in the rhymer
        // Long press on the left part of the EditText, to select the first word
        String firstWord = poemText.substring(0, poemText.indexOf(' ')).toLowerCase(Locale.getDefault());
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));

        // Select the "rhymer" popup
        mDevice.findObject(By.text("Rhymer")).click();
        checkTitleStripCenterTitle(mActivityTestRule.getActivity(), R.string.tab_rhymer);
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)));

        // Look up in the thesaurus
        swipeViewPagerLeft(3);
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        mDevice.findObject(By.text("Thesaurus")).click();
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)));

        // Look up in the dictionary
        swipeViewPagerLeft(2);
        onView(withId(R.id.tv_text)).perform(longTap(1, 0));
        mDevice.findObject(By.text("Dictionary")).click();
        onView(allOf(withId(R.id.tv_list_header), isDisplayed())).check(matches(withText(firstWord)));
    }

}
