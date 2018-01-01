package visuitest;

import com.github.czyzby.kiwi.util.tuple.immutable.Pair;
import com.github.czyzby.kiwi.util.tuple.immutable.Triple;
import com.github.czyzby.kiwi.util.tuple.mutable.MutablePair;
import com.sun.org.apache.bcel.internal.generic.IFEQ;

import java.util.ArrayList;
import java.util.Arrays;

import static com.badlogic.gdx.math.MathUtils.random;

public class Game {
    static int active_player;
    static int selected_prov = 0;

    public static void load_gamedata() {
        Data.init_test_gamedata();
    }

    public static class Data {
        static int no_players;
        static Player_Data[] player_data;
        static Prov_Data[] prov_data;

        public static int who_owns(int prov_id) {
            int i = -1;
            outerloop:
            for (Player_Data player : player_data) {
                if (player.owns_provs.contains(prov_id)) {
                    i = player.player_id;
                    break outerloop;
                }
            }
            assert i != -1 || prov_id == -1;
            return i;
        }

        public static void transfer_prov_conquest(int prov_id, int new_owner_id, int new_armies_num) {
            delete_ownership(prov_id);
            player_data[new_owner_id].owns_provs.add(prov_id);
            set_num_armies(prov_id, new_armies_num);
        }

        public static void init_test_gamedata() {
            basic_load(3);
            for (int i = 0; i < prov_data.length; i++) {
                //divides the provs among the players and gives each prov 5 armies
                if (i == 0) {
                    player_data[i].owns_provs.add(i);
                } else {
                    player_data[i % no_players].owns_provs.add(i);
                }
                prov_data[i].no_armies = 5;
            }
        }

        public static void attack_a_round(int prov_from, int prov_to/*, int no_armies*/) {
            // does 1 round of attacking
            // if either party loses at the end at the end of the round it ends
            // else it opens another window

            assert who_owns(prov_from) != who_owns(prov_to);
            //assert !(no_armies > get_num_armies(prov_from)-1);//you always need to leave behind at least one army

            int attacking_armies = get_num_armies(prov_from) - 1;//TODO allow the attacker to choose the number of armies to use
            int defense_armies = get_num_armies(prov_to);

            int defender_losses = 0;
            int attacker_losses = 0;


            Pair defense_roll = defense_roll(get_num_armies(prov_to));
            Pair offensive_roll = offensive_roll(get_num_armies(prov_from));

            if ((int) offensive_roll.getFirst() > (int) defense_roll.getFirst()) {
                defense_armies -= 1;
                //System.out.print("defense lost now has "+ defense_armies+"\n");//debug
                defender_losses++;
            } else {
                attacking_armies -= 1;
                //System.out.print("offence lost now has "+ get_num_armies(prov_from)+"\n");//debug
                attacker_losses++;

            }
            if (attacking_armies > 0 && defense_armies > 0
                    && (int) offensive_roll.getSecond() > 0 && (int) defense_roll.getSecond() > 0
                /*doesn't resolve the second pair of dice if it wasn't rolled*/) {
                if ((int) offensive_roll.getSecond() > (int) defense_roll.getSecond()) {
                    defense_armies -= 1;
                    //System.out.print("defense lost now has "+ defense_armies+"\n");//debug
                    defender_losses++;
                } else {
                    attacking_armies -= 1;
                    //System.out.print("offence lost now has "+attacking_armies+"\n");//debug
                    attacker_losses++;
                }
            }


            set_num_armies(prov_from, attacking_armies + 1);//TODO allow the attacker to choose the number of armies to use
            set_num_armies(prov_to, defense_armies);

            //calls the ui windows
            if (attacking_armies > 1 && defense_armies > 1) {
                //System.out.print("attack continues\n");
                UI.attack_window.continue_attack_window(prov_from, prov_to, defender_losses, attacker_losses);
            } else {
                if (attacking_armies > 0) {//if attacker wins
                    transfer_prov_conquest(prov_to, who_owns(prov_from), attacking_armies);
                    //System.out.print("attacker wins\n");
                    set_num_armies(prov_from, 1);
                    UI.attack_window.attack_end_screeen(
                            prov_from, prov_to, defender_losses, attacker_losses, true);
                } else {//if defender wins
                    //System.out.print("defender wins\n");
                    UI.attack_window.attack_end_screeen(
                            prov_from, prov_to, defender_losses, attacker_losses, false);
                }
            }
        }

        public static boolean is_connected(int prov_id1, int prov_id2) {
            for (int connection : prov_data[prov_id1].connnections) {
                if (connection == prov_id2) {
                    return true;
                }
            }
            return false;
        }

        public static int get_num_armies(int prov_id) {
            return prov_data[prov_id].no_armies;
        }


        protected static void tmp_reinforce() {//TODO not final class just for testing
            for (Integer owned : player_data[active_player].owns_provs) {
                prov_data[owned].no_armies += 3;
            }
            System.out.print("reinforced player " + active_player + "\n");
        }


        private static void tmp_check_for_victory(){
            //todo checks if someone owns the entire board
        }

        private static void set_num_armies(int target_prov_id, int num_armies) {
            prov_data[target_prov_id].no_armies = num_armies;
        }

        private static void move_armies(int prov_id, int armies) {
            //TODO for movement fase
        }

        private static void add_armies(int target_prov_id, int num_armies) {
            if (get_num_armies(target_prov_id) < 0) {
                prov_data[target_prov_id].no_armies = 0;
            }
            prov_data[target_prov_id].no_armies += num_armies;
        }

        private static Pair defense_roll(int num_armies) {//TODO allow the player to choose how many dice to use
            assert num_armies > 0;
            MutablePair dice = new MutablePair(0, 0);
            if (num_armies > 1) {
                int roll_1 = random(1, 6);
                int roll_2 = random(1, 6);
                if (roll_1 > roll_2) {//organises them from highest to lowest
                    dice.setFirst(roll_1);
                    dice.setSecond(roll_2);
                } else {
                    dice.setFirst(roll_2);
                    dice.setSecond(roll_1);
                }
            } else {
                dice.setFirst(random(1, 6));
            }
            return dice.toImmutable();
        }

        private static Pair offensive_roll(int num_armies) {
            //rolls 3 dice but only the first 2 matter
            MutablePair dice = new MutablePair(0, 0);
            if (num_armies > 2) {
                int[] rolls = new int[]{random(1, 6), random(1, 6), random(1, 6)};
                Arrays.sort(rolls);//sorts from smallest to largest
                dice.setFirst(rolls[2]);
                dice.setSecond(rolls[1]);
            } else if (num_armies == 2) {
                int roll_1 = random(1, 6);
                int roll_2 = random(1, 6);
                if (roll_1 > roll_2) {//organises them from highest to lowest
                    dice.setFirst(roll_1);
                    dice.setSecond(roll_2);
                } else {
                    dice.setFirst(roll_2);
                    dice.setSecond(roll_1);
                }
            } else {
                dice.setFirst(random(1, 6));
            }
            return dice.toImmutable();
        }

        private static void delete_ownership(int prov_id) {
            int removed_counter = 0;
            outerloop:
            for (Player_Data player : player_data) {
                for (int i = 0; i < player.owns_provs.size(); i++) {
                    if (player.owns_provs.get(i) == prov_id) {
                        player.owns_provs.remove(i);
                    }
                }
            }
            assert removed_counter < 2;
        }

        private static void basic_load(int no_players_arg) {//no_player needs to be set before using
            //generates the basic structure you always need
            no_players = no_players_arg;

            //generates empty player_data_set with just id's
            player_data = new Player_Data[no_players];
            for (int i = 0; i < player_data.length; i++) {
                player_data[i] = new Player_Data(i);
            }
            //generates province data without armies
            prov_data = new Prov_Data[Assets.no_provs];
            int[][] navtree = Assets.navtree;

            for (int i = 0; i < prov_data.length; i++) {
                prov_data[i] = new Prov_Data(i, navtree[i]);

            }
        }


        private static class Player_Data {
            int player_id;
            ArrayList<Integer> owns_provs;

            Player_Data(int player_id_arg) {
                player_id = player_id_arg;
                owns_provs = new ArrayList<Integer>();
            }

            Player_Data(int player_id_arg, ArrayList<Integer> owns_provs_arg) {
                player_id = player_id_arg;
                owns_provs = owns_provs_arg;
            }
        }

        private static class Prov_Data {
            int prov_id;
            int no_armies;
            int[] connnections;

            public Prov_Data(int prov_id_arg, int[] connnections_arg) {
                prov_id = prov_id_arg;
                connnections = connnections_arg;
                no_armies = 0;
            }

            public Prov_Data(int prov_id_arg, int no_armies_arg, int[] connnections_arg) {
                prov_id = prov_id_arg;
                no_armies = no_armies_arg;
                connnections = connnections_arg;
            }
        }

    }

    public static class State {
        // state affects the game in two ways
        // first there is imidiate changes like ui
        // the second part is that changing the game state changes what you can and can't do

        public static int game_state = 1;//TODO implement setup
        public static int turn_state = -0;

        public static class Game_State{

            static void update() {// todo implement
                if (game_state < 2) {
                    game_state++;
                } else {
                    game_state = 1;// 0 is init state
                }
                switch (game_state) {
                    case 0:
                        setup();
                    case 1:
                        start_game();
                    case 2:
                        victory_screen();
                }
            }

            private static void setup() {
            }

            private static void start_game() {
            }

            private static void victory_screen() {

            }
        }

        public static class Turn_State{

            public static String get_turn_phase_name(){
                switch (turn_state) {
                    case -1:
                        return new String("setup phase");
                    case 0:
                        return new String("Reinforcement phase");
                    case 1:
                        return new String("Attack phase");
                    default:
                        return new String("Movement phase");
                }
            }

            public static void advance_turn_state() {//keeps track of the turn phase
                // Increments state
                if (turn_state< 2) {
                    turn_state++;
                    update();
                } else {
                    end_player_turn();
                }
            }

            public static void end_player_turn(){
                turn_state = 0;
                if (active_player < Data.no_players-1){
                    active_player++;
                }else {
                    active_player = 0;
                }
                update();
            }

            private static void update(){
                // Updates game
                switch (turn_state) {
                    case 0:
                        reinforcement_phase();
                    case 1:
                        attack_phase();
                    case 2:
                        movement_phase();
                }
            }

            private static void reinforcement_phase() {
                UI.info_bar.update_player_label();
                UI.info_bar.update_turn_phase_label();
                UI.info_bar.set_default_dialog();
                UI.reinforcement_window.start_turn();
                // show player ready dialog
                // reinforce window y/n
                    // if yes then it reinforces and skips straigt to the next player
                // advance state
                //TODO add continents
            }

            private static void attack_phase() {
                UI.info_bar.update_turn_phase_label();
                UI.info_bar.set_default_dialog();

                // enter advances state
            }

            private static void movement_phase() {
                UI.info_bar.update_turn_phase_label();
                UI.info_bar.set_default_dialog();

                // enter advances state
            }

        }
    }

}
