package visuitest;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;

public class UI {
    static Stage stage;
    static Tooltip prov_info;
    static private Skin skin;
    static private MenuBar menuBar;
    static private Confirm_Attack confirm_attack_window;
    static private boolean confirm_attack_window_is_opened;

    static Reinforcement_Phase_window reinforcement_window;
    static Attack_Phase attack_window;
    static Info_Bar info_bar;//todo add
    static boolean disable_map_layer = false;

    //TODO add victory screen
    //TODO add setup screen
    //TODO add card screen
    //TODO add menubar for saving/loading game?
    public static void init_UI() {//todo cleanup
	VisUI.load();
        stage = new Stage(new ScreenViewport());
        skin = VisUI.getSkin();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        menuBar = new MenuBar();
        root.add(menuBar.getTable()).growX().row();
        root.add().grow();
        //todo figure out what grow method does

        create_menu_bar();

        info_bar = new Info_Bar();//auto adds the actors to the stage

        prov_info = new Prov_Info_Tooltip();
        stage.addActor(prov_info);

        reinforcement_window = new Reinforcement_Phase_window();
        stage.addActor(reinforcement_window);

        attack_window = new Attack_Phase();
        stage.addActor(attack_window);

        confirm_attack_window = new Confirm_Attack();
        stage.addActor(confirm_attack_window);
    }
    public static class Info_Bar{
        // Sits at the bottom of the screen and provides game info
        private VisLabel active_player_label = new VisLabel();// Sits on the left. Shows the active player
        private VisLabel dialog_label = new VisLabel(); // Sits in the middle. Communicates game info to the player,
        // like you can't attack cuz no patch
        private VisLabel phase_label = new VisLabel(); // Sits on the right. Displays the current turn phase.

        private Float dialog_timer = 0f; // records the time since the last time the dialog was changed
        private static int dialog_display_secs = 5; // the num of secs before the dialog
        private boolean timer_active = false;

        public int info_bar_height;


        public Info_Bar(){
            stage.addActor(active_player_label);
            stage.addActor(dialog_label);
            stage.addActor(phase_label);

            update_player_label();
            update_turn_phase_label();
            set_default_dialog();
            info_bar_height = (int)dialog_label.getHeight();

            Assets.render_infobar_background_bool = true;
        }

        public void update_player_label(){
            active_player_label.setText("Player "+(Game.active_player + 1)+"'s turn");
            active_player_label.pack();
            active_player_label.setPosition(0,0);
        }

        public void update_turn_phase_label(){
            phase_label.setText(Game.State.Turn_State.get_turn_phase_name());
            phase_label.pack();
            phase_label.setPosition(stage.getWidth()-phase_label.getWidth(),0);
        }

        public void push_dialog(String message){//pushes a string to the dialog box and sets the timer
            dialog_label.setText(message);
            dialog_label.pack();
            dialog_label.setPosition((stage.getWidth()/2) - (dialog_label.getWidth()/2),0);
            timer_active = true;
            dialog_timer = 0f;

        }

        public void set_default_dialog() {

            if (Game.State.turn_state < 2) {
                dialog_label.setText("Press enter to end " + Game.State.Turn_State.get_turn_phase_name());
            } else {
                dialog_label.setText("Press enter to end turn");
            }
            dialog_label.pack();
            dialog_label.setPosition((stage.getWidth() / 2) - (dialog_label.getWidth() / 2), 0);
        }

        public void update_dialog_timer(Float time_since_last_frame){
            if (timer_active){
                dialog_timer += time_since_last_frame;
                if (dialog_timer > (float)dialog_display_secs){
                    set_default_dialog();
                }
            }
        }
    }

    private static void create_menu_bar () {
        //todo add card menue
        Menu active_player_menu = new Menu("select active player");
        Menu reinforce_button =  new Menu("reinforce");
        reinforce_button.setVisible(false);//makes the popupmenu invisible and turns in into a normal button
        //TODO doesn't acually turn it into a normal button but not important cuz debug only

        active_player_menu.addItem(new MenuItem("player 1", new ChangeListener() {
		@Override
		public void changed (ChangeEvent event, Actor actor) {
		    Game.active_player = 0;
		    System.out.print("The active player is "+Game.active_player+"\n");
		}
	    }));

        active_player_menu.addItem(new MenuItem("player 2", new ChangeListener() {
		@Override
		public void changed (ChangeEvent event, Actor actor) {
		    Game.active_player = 1;
		    System.out.print("The active player is "+Game.active_player+"\n");
		}
	    }));

        active_player_menu.addItem(new MenuItem("player 3", new ChangeListener() {
		@Override
		public void changed (ChangeEvent event, Actor actor) {
		    Game.active_player = 2;
		    System.out.print("The active player is "+Game.active_player+"\n");
		}
	    }));

        reinforce_button.openButton.addListener(new ChangeListener() {
		@Override
		public void changed (ChangeEvent event, Actor actor) {//TODO temporary
		    Game.Data.tmp_reinforce();
		}
	    });


        menuBar.addMenu(active_player_menu);
        menuBar.addMenu(reinforce_button);

    }

    private static class Prov_Info_Tooltip extends Tooltip{
        public Prov_Info_Tooltip(){
            setVisible(false);
            setAppearDelayTime(0f);
            setFadeTime(0f);
        }
    }

    public static void set_prov_tooltip(int prov_id, int screenX, int screenY) {
        if (disable_map_layer){
            prov_info.setVisible(false);
        }else {
            screenY = Assets.scr_height-screenY;
            prov_info.setPosition((float)screenX, (float)screenY);
            if (Game.Data.who_owns(prov_id) == Game.active_player){
                Label content = new Label(Assets.get_prov_name(prov_id)+"\n"+
                        "owned by you\n"+
                        "contains "+ Game.Data.get_num_armies(prov_id)+" armies"
                        ,VisUI.getSkin());
                prov_info.setContent(content);
            }else{

                Label content = new Label(Assets.get_prov_name(prov_id)+"\n"+
                        "owned by player "+ (Game.Data.who_owns(prov_id)+1)+"\n"+
                        "contains "+ Game.Data.get_num_armies(prov_id)+" armies"
                        ,VisUI.getSkin());
                prov_info.setContent(content);
            }

        }

    }

    public static void call_confirm_attack_window(int prov_from,int prov_to ){
        if (!(confirm_attack_window_is_opened) && !disable_map_layer){
            confirm_attack_window.open(prov_from, prov_to);
        }
    }

    private static Button get_button(String button_text){
        Button button = new Button(VisUI.getSkin());
        button.add(button_text);
        button.pad(5f, 5f, 5f, 5f);
        return button;
    }

    private static void get_label_window(UI_Window arg, String label_text){
        disable_map_layer = true;

        VisLabel label = new VisLabel(label_text ,Align.center);
        arg.add(label).row();

        arg.pad(10f, 10f, 10f, 10f);
        arg.pack();
        arg.centerWindow();
        arg.setVisible(true);
    }

    private static class Confirm_Attack extends UI_Window {
        //TODO redesign cuz it's shit
        private void open(int prov_from,int prov_to ){
            confirm_attack_window_is_opened = true;

            get_label_window(this,"You are about to attack "+Assets.get_prov_name(prov_from)
                    +" from "+ Assets.get_prov_name(prov_to) + "\n you have " + Game.Data.get_num_armies(prov_from)
                    +" armies and they have "+ Game.Data.get_num_armies(prov_to)
                    +" armies.\n\n are you sure you want to continue?");

            Button yes_button = get_button("yes");
            yes_button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    //UI.attack_window.intitial_attack_window(prov_from,prov_to);
                    UI.attack_window.intitial_attack_window(prov_from,prov_to);
                    disable_map_layer =true;
                    confirm_attack_window_is_opened =false;
                    setVisible(false);
                    clearChildren();
                }
            });

            //pad(1f);
            Button no_button = get_button("no");
            no_button.addListener(new ChangeListener() {
                @Override
                public void changed (ChangeEvent event, Actor actor) {
                    info_bar.push_dialog("test");
                    confirm_attack_window_is_opened =false;
                    setVisible(false);
                    clearChildren();
                }
            });

            Table table = new Table(VisUI.getSkin());
            table.add(no_button).padRight(15f);
            table.add(yes_button);

            add(table).padTop(15);
            pack();
            centerWindow();

        }
    }

    protected static class Attack_Phase extends UI_Window {//todo improve text
        int defender_losses_total = 0;
        int attacker_losses_total = 0;

        private void intitial_attack_window(int prov_from, int prov_to) {
            //this gets opened when you fist initate an attack

            Button attack_button = get_button("attack");
            attack_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                    Game.Data.attack_a_round(prov_from, prov_to);
                }
            });

            get_label_window(this,"you have " + Game.Data.get_num_armies(prov_from) + " armies" +
					  "\n\nthe enemy has " + Game.Data.get_num_armies(prov_to) + " armies.\n");
            add(attack_button);
            pack();
            centerWindow();

        }

        public void continue_attack_window(int prov_from, int prov_to
					   , int defender_losses_arg, int attacker_losses_arg) {

            // this is the window once at least one round of combat has taken place
            // but no one has won yet.
            defender_losses_total+=defender_losses_arg;
            attacker_losses_total+=attacker_losses_arg;

            get_label_window(this, "you have " + Game.Data.get_num_armies(prov_from) + " armies" +
					  "\n\nthe enemy has " + Game.Data.get_num_armies(prov_to) + " armies.\n");

            Button attack_button = get_button("continue attack");
            attack_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                    Game.Data.attack_a_round(prov_from, prov_to);

                }
            });

            Button retreat_button = get_button("retreat");

            retreat_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                }
            });

            Table buttontable = new Table(VisUI.getSkin());
            buttontable.add(retreat_button).padRight(15f);
            buttontable.add(attack_button);
            add(buttontable);
            pack();
            centerWindow();

        }

        public void attack_end_screeen(int prov_from, int prov_to
				       , int defender_losses_arg, int attacker_losses_arg, boolean victory) {
            // this is the window that is displayed when either side has won
            defender_losses_total+=defender_losses_arg;
            attacker_losses_total+=attacker_losses_arg;

            if (victory){
                get_label_window(this, "You conquered " + Assets.get_prov_name(prov_from)
                        + "\nyou lost "+attacker_losses_total+ " armies and have "+Game.Data.get_num_armies(prov_from)+" armies remaining "
                );
            }else {
                get_label_window(this,"You failed to conquer " + Assets.get_prov_name(prov_from)
                        + "\nyou lost "+attacker_losses_total+ " armies and have "+Game.Data.get_num_armies(prov_from)+" armies remaining \n" +
                        "the enemy lost "+defender_losses_total+" armies and has "+Game.Data.get_num_armies(prov_to)+" armies remaining"
                );
            }

            Button ok_button = get_button("ok");
            ok_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                    UI.disable_map_layer = false;
                }
            });

            add(ok_button);
            pack();
            defender_losses_total = 0;
            attacker_losses_total = 0;

        }
    }

    protected static class Reinforcement_Phase_window extends UI_Window {

        public void start_turn(){
            Button ok_button = get_button("ok");
            ok_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                    UI.disable_map_layer = false;
                    place_continent_reinforcements();
                }
            });

            get_label_window(this, "player "+ Game.active_player +"'s turn");
            add(ok_button);
            pack();
            centerWindow();

        }

        private void place_continent_reinforcements(){
            reinforement_round();
            //TODO stub
        }

        private void reinforement_round(){

            Button yes_button = get_button("yes");
            yes_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                    UI.disable_map_layer = false;
                    Game.Data.tmp_reinforce();
                    player_reinforced_window();
                }
            });

            Button no_button = get_button("no");
            no_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                    UI.disable_map_layer = false;
                    Game.State.Turn_State.advance_turn_state();
                }
            });


            get_label_window(this, "player "+ Game.active_player +
                    "\n Would you like to reinforce? \nthis will end your turn..");

            Table buttontable = new Table(VisUI.getSkin());
            buttontable.add(yes_button).padRight(15f);
            buttontable.add(no_button);
            add(buttontable);
            pack();
            centerWindow();
        }

        private void player_reinforced_window(){

            Button ok_button = get_button("end turn");
            ok_button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    setVisible(false);
                    clearChildren();
                    UI.disable_map_layer = false;
                    Game.State.Turn_State.end_player_turn();
                }
            });

            get_label_window(this, "you have been reinforced");

            add(ok_button);
            pack();
            centerWindow();
        }
    }
    private static class Setup_menu{
        //todo
        private static void bypass(){

        }
    }

    private static class UI_Window extends VisWindow {
        //TODO redesign cuz it's shit
        private UI_Window() {
            super("");
            getTitleLabel().setWidth(1f);
            setVisible(false);
        }
    }
}
