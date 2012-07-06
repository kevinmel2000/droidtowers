/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;
import com.happydroids.droidtowers.Colors;
import com.happydroids.droidtowers.Strings;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.input.InputCallback;
import com.happydroids.droidtowers.input.InputSystem;

import static com.happydroids.droidtowers.ColorUtil.rgba;
import static com.happydroids.droidtowers.gui.FontManager.Roboto18;
import static com.happydroids.droidtowers.gui.FontManager.Roboto32;
import static com.happydroids.droidtowers.platform.Display.scale;

public class TowerWindow {
  private static final int[] DIALOG_CLOSE_KEYCODES = new int[]{InputSystem.Keys.ESCAPE, InputSystem.Keys.BACK};

  private InputCallback closeDialogCallback;
  private Runnable dismissCallback;
  protected final Stage stage;
  protected Table content;
  protected Table wrapper;
  private final Label titleLabel;
  protected final TransparentTextButton closeButton;
  private Actor staticHeaderContent;
  private Actor staticFooterContent;
  private final Cell actionBarCell;
  private final Cell footerBarCell;
  private final Cell contentRow;
  protected final VerticalRule closeButtonLine;

  public TowerWindow(String title, Stage stage) {
    this.stage = stage;

    wrapper = new Table() {
//      TODO: GROT, I have no clue why this texture is blending with the stuff behind it..
      @Override
      protected void drawBackground(SpriteBatch batch, float parentAlpha) {
        batch.disableBlending();
        super.drawBackground(batch, 1f);
        batch.enableBlending();
      }
    };
    wrapper.setFillParent(true);
    wrapper.defaults().top().left();
    wrapper.touchable = true;
    wrapper.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {

      }
    });

    Texture texture = TowerAssetManager.texture("hud/window-bg.png");
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    wrapper.setBackground(new NinePatch(texture));
    wrapper.size((int) stage.width(), (int) stage.height());

    titleLabel = Roboto32.makeLabel(Strings.truncate(title, 40));
    closeButton = Roboto18.makeTransparentButton("< back", rgba("#007399"), Colors.DARK_GRAY);
    closeButtonLine = new VerticalRule(scale(2));

    Table topBar = new Table();
    topBar.row().fill();
    topBar.add(closeButton).fill();
    topBar.add(closeButtonLine).fillY();
    topBar.add(titleLabel).center().left().expand().pad(scale(4)).padLeft(scale(12));

    wrapper.add(topBar).fill();

    wrapper.row().fillX();
    wrapper.add(new HorizontalRule(scale(2))).expandX();

    wrapper.row().fillX();
    actionBarCell = wrapper.add();

    contentRow = wrapper.row();
    contentRow.fill().padLeft(scale(24)).padRight(scale(24));
    wrapper.add(makeContentContainer()).expand();

    wrapper.row().fillX();
    footerBarCell = wrapper.add();

    closeButton.setClickListener(new VibrateClickListener() {
      @Override
      public void onClick(Actor actor, float x, float y) {
        dismiss();
      }
    });
  }

  protected Actor makeContentContainer() {
    content = new Table();
    content.defaults().top().left();
    content.row().fill();

    return content;
  }

  public Cell add(Actor actor) {
    return content.add(actor);
  }

  public Cell row() {
    return content.row();
  }

  public TowerWindow show() {
    bindKeys();

    wrapper.invalidate();
    wrapper.pack();
    wrapper.color.set(Color.WHITE);
    stage.addActor(wrapper);

    return this;
  }

  public void dismiss() {
    wrapper.visible = false;
    unbindKeys();

    if (dismissCallback != null) {
      dismissCallback.run();
    }

    stage.setScrollFocus(null);
    stage.setKeyboardFocus(null);

    wrapper.markToRemove(true);
  }

  public TowerWindow setDismissCallback(Runnable dismissCallback) {
    this.dismissCallback = dismissCallback;

    return this;
  }

  protected void debug() {
    content.debug();
  }

  protected void clear() {
    content.clear();
  }

  protected void bindKeys() {
    closeDialogCallback = new InputCallback() {
      public boolean run(float timeDelta) {
        TowerWindow.this.dismiss();
        return true;
      }
    };

    InputSystem.instance().bind(DIALOG_CLOSE_KEYCODES, closeDialogCallback);
  }

  protected void unbindKeys() {
    if (closeDialogCallback != null) {
      InputSystem.instance().unbind(DIALOG_CLOSE_KEYCODES, closeDialogCallback);
      closeDialogCallback = null;
    }
  }

  public TowerWindow setTitle(String title) {
    titleLabel.setText(title);

    return this;
  }

  protected Cell defaults() {
    return content.defaults();
  }

  protected Cell add() {
    return content.add();
  }

  public void setStaticHeader(Actor staticContent) {
    this.staticHeaderContent = staticContent;
    actionBarCell.setWidget(staticContent);
    actionBarCell.expandX();
    wrapper.pack();
  }

  public void setStaticFooter(Actor staticContent) {
    this.staticFooterContent = staticContent;
    footerBarCell.setWidget(staticContent);
    footerBarCell.expandX();
    wrapper.pack();
  }

  protected Cell addHorizontalRule(Color darkGray, int desiredHeight, int colspan) {
    row().fillX();
    return add(new HorizontalRule(darkGray, desiredHeight)).expandX().colspan(colspan);
  }

  protected Cell addLabel(String labelText, FontManager labelFont, Color fontColor) {
    row();
    return add(labelFont.makeLabel(labelText, fontColor));
  }

  public Cell addLabel(String text, FontManager labelFont) {
    return addLabel(text, labelFont, Color.WHITE);
  }
}
