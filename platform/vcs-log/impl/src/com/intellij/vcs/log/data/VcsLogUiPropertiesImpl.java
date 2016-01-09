/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.vcs.log.data;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.vcs.log.VcsLogSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Stores UI configuration based on user activity and preferences.
 * Differs from {@link VcsLogSettings} in the fact, that these settings have no representation in the UI settings,
 * and have insignificant effect to the logic of the log, they are just gracefully remember what user prefers to see in the UI.
 */
@State(name = "Vcs.Log.UiProperties", storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)})
public class VcsLogUiPropertiesImpl implements PersistentStateComponent<VcsLogUiPropertiesImpl.State>, VcsLogUiProperties {
  private static final int RECENTLY_FILTERED_VALUES_LIMIT = 10;
  private final VcsLogSettings mySettings;
  private State myState = new State();

  public static class State {
    public boolean SHOW_DETAILS = true;
    public boolean SHOW_BRANCHES_PANEL = false;
    public boolean LONG_EDGES_VISIBLE = false;
    public int BEK_SORT_TYPE = 0;
    public boolean SHOW_ROOT_NAMES = false;
    public Deque<UserGroup> RECENTLY_FILTERED_USER_GROUPS = new ArrayDeque<UserGroup>();
    public Deque<UserGroup> RECENTLY_FILTERED_BRANCH_GROUPS = new ArrayDeque<UserGroup>();
    public Map<String, Boolean> HIGHLIGHTERS = ContainerUtil.newTreeMap();
  }

  public VcsLogUiPropertiesImpl(@NotNull VcsLogSettings settings) {
    mySettings = settings;
  }

  @Nullable
  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(State state) {
    myState = state;
    if (mySettings.isShowBranchesPanel()) {
      myState.SHOW_BRANCHES_PANEL = true;
      mySettings.setShowBranchesPanel(false);
    }
  }

  /**
   * Returns true if the details pane (which shows commit meta-data, such as the full commit message, commit date, all references, etc.)
   * should be visible when the log is loaded; returns false if it should be hidden by default.
   */
  @Override
  public boolean isShowDetails() {
    return myState.SHOW_DETAILS;
  }

  @Override
  public void setShowDetails(boolean showDetails) {
    myState.SHOW_DETAILS = showDetails;
  }

  @Override
  public void addRecentlyFilteredUserGroup(@NotNull List<String> usersInGroup) {
    addRecentGroup(usersInGroup, myState.RECENTLY_FILTERED_USER_GROUPS);
  }

  @Override
  public void addRecentlyFilteredBranchGroup(@NotNull List<String> valuesInGroup) {
    addRecentGroup(valuesInGroup, myState.RECENTLY_FILTERED_BRANCH_GROUPS);
  }

  private static void addRecentGroup(@NotNull List<String> valuesInGroup, @NotNull Deque<UserGroup> stateField) {
    UserGroup group = new UserGroup();
    group.users = valuesInGroup;
    if (stateField.contains(group)) {
      return;
    }
    stateField.addFirst(group);
    if (stateField.size() > RECENTLY_FILTERED_VALUES_LIMIT) {
      stateField.removeLast();
    }
  }

  @Override
  @NotNull
  public List<List<String>> getRecentlyFilteredUserGroups() {
    return getRecentGroup(myState.RECENTLY_FILTERED_USER_GROUPS);
  }

  @Override
  @NotNull
  public List<List<String>> getRecentlyFilteredBranchGroups() {
    return getRecentGroup(myState.RECENTLY_FILTERED_BRANCH_GROUPS);
  }

  @NotNull
  private static List<List<String>> getRecentGroup(Deque<UserGroup> stateField) {
    return ContainerUtil.map2List(stateField, new Function<UserGroup, List<String>>() {
      @Override
      public List<String> fun(UserGroup group) {
        return group.users;
      }
    });
  }

  @Override
  public boolean areLongEdgesVisible() {
    return myState.LONG_EDGES_VISIBLE;
  }

  @Override
  public void setLongEdgesVisibility(boolean visible) {
    myState.LONG_EDGES_VISIBLE = visible;
  }

  @Override
  public int getBekSortType() {
    return myState.BEK_SORT_TYPE;
  }

  @Override
  public void setBek(int bekSortType) {
    myState.BEK_SORT_TYPE = bekSortType;
  }

  @Override
  public boolean isShowRootNames() {
    return myState.SHOW_ROOT_NAMES;
  }

  @Override
  public void setShowRootNames(boolean isShowRootNames) {
    myState.SHOW_ROOT_NAMES = isShowRootNames;
  }

  @Override
  public boolean isHighlighterEnabled(@NotNull String id) {
    Boolean result = myState.HIGHLIGHTERS.get(id);
    return result != null ? result : true; // new highlighters get enabled by default
  }

  @Override
  public void enableHighlighter(@NotNull String id, boolean value) {
    myState.HIGHLIGHTERS.put(id, value);
  }

  @Override
  public boolean isShowBranchesPanel() {
    return myState.SHOW_BRANCHES_PANEL;
  }

  @Override
  public void setShowBranchesPanel(boolean show) {
    myState.SHOW_BRANCHES_PANEL = show;
  }

  public static class UserGroup {
    public List<String> users = new ArrayList<String>();

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      UserGroup group = (UserGroup)o;
      if (!users.equals(group.users)) return false;
      return true;
    }

    @Override
    public int hashCode() {
      return users.hashCode();
    }
  }

}
