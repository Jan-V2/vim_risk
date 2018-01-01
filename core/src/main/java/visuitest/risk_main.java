package visuitest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.github.czyzby.kiwi.util.tuple.immutable.Pair;
import com.badlogic.gdx.Input.Keys;

public class risk_main extends ApplicationAdapter {
    
//TODO figure out game screens
//TODO add continents
//TODO add cards
//TODO write load game data
//TODO write turn logic including ui changes
//TODO write victory conditions
//TODO write setup game wizard
//TODO write save game data
//TODO write basic ai?
//TODO increase resolution to 720p
//TODO improve visuals

    @Override
    public void create(){
	Assets.load();
	Game.load_gamedata();
	//sets background
	Gdx.gl.glClearColor(Assets.sea_color_float[0], Assets.sea_color_float[1], Assets.sea_color_float[2], Assets.sea_color_float[3]);

	UI.init_UI();
	MyInputProcessor inputProcessor = new MyInputProcessor();
	InputMultiplexer multiplexer = new InputMultiplexer(UI.stage,inputProcessor);//order important
	Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Assets.render();

		UI.info_bar.update_dialog_timer(Gdx.graphics.getDeltaTime());
		//System.out.println(Gdx.graphics.getFramesPerSecond());
        UI.stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f));
        UI.stage.draw();

    }

    public class MyInputProcessor implements InputProcessor {

	@Override
	public boolean keyDown(int keycode) {
		//System.out.println(keycode);
		if (keycode == Keys.ENTER){
			Game.State.Turn_State.advance_turn_state();
		}
	    return true;
	}
	@Override
	public boolean keyUp(int keycode) {
	    return false;
	}
	@Override
	public boolean keyTyped(char character) {
	    return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
	    //to make room for the button bar
	    int scr_height = Gdx.graphics.getHeight();
	    int scr_width = Gdx.graphics.getWidth();
	    int hexes_per_column = (scr_height - Assets.halfhex) / (Assets.hex_size);
	    int hexes_per_row = scr_width / (Assets.halfhex+Assets.quarthex);

	    //to filter out negative numbers
	    if (screenX < 0 || screenY-30 < 0){
		return true;
	    }

	    Pair clicked_hex = resolve_hex(screenX,screenY);

	    int clicked_prov_id;

	    //dumps coords if it's outside of the grid
	    if ((int)clicked_hex.getFirst()<0||(int)clicked_hex.getSecond()<0||
		(int)clicked_hex.getFirst()>=hexes_per_row-1||(int)clicked_hex.getSecond()>=hexes_per_column-1){
		clicked_prov_id = -1;
	    }else {
		clicked_prov_id = Assets.resolve_prov_id(clicked_hex);
	    }


            mouse_click_handeler(clicked_prov_id, screenX, screenY);
	    return true;
	}

	private Pair resolve_hex(int screenX,int screenY){
	    //resolves the hex coordinates from mouse coordinates


	    int grid_offsetX = 0; //(scr_width % (halfhex+quarthex)) / 4;//TODO ofset set to 0 for debugging
	    int grid_offsetY = 30; //((scr_height - halfhex) % (hexsize))/ 2;//why do i have to divide by 4?

	    screenX = screenX - grid_offsetX;
	    screenY = screenY - grid_offsetY; //first resolve column then row. if column is even then -halfhex

	    Double tmp1 = new Double((screenX / Assets.quarthex));
	    int quarthex_X = tmp1.intValue();
	    int hex_grid_X = quarthex_X / 3;//used both as final result and as full_hex_x

	    Double tmp2 = new Double((screenY / Assets.halfhex +0.5f));
	    int halfhex_Y = tmp2.intValue();
	    int hex_grid_Y = halfhex_Y / 2;

		int X_remain;
		int Y_remain;
		if (quarthex_X > 0)	{// to prevent divison / 0
			X_remain = screenX % (quarthex_X*Assets.quarthex);
	    } else	{
			X_remain = screenX;
		}

		if (halfhex_Y > 0)	{// to prevent divison / 0
			Y_remain = screenY % (halfhex_Y*Assets.halfhex);
		} else	{
			Y_remain = screenY;
		}

		int X_offset;
		int Y_offset;
		//calculates x offset
		if (quarthex_X % 3 > 0)	{//if it is in a straight collumn
			X_offset = 0;
		} else	{
			X_offset = X_grid_offset_calc(X_remain, Y_remain, halfhex_Y, hex_grid_X, Assets.quarthex);
		}

		// calculates y offset
		if (halfhex_Y % 2 == 0)	{ //not sure what this line does, but it was in the origanal code.
			if (quarthex_X % 3 > 0)	{//if it's in a sqaure row
				if (hex_grid_X % 2 == 0)	{
				Y_offset = 0;
			} else	{
				Y_offset = -1;
			}
			} else {
				Y_offset = Y_grid_offset_calc (X_remain, Y_remain, hex_grid_X, Assets.quarthex);
			}
		} else	{
			Y_offset = 0;
		}
		hex_grid_Y = hex_grid_Y + Y_offset;
		hex_grid_X = hex_grid_X + X_offset;

	    return new Pair(hex_grid_X,hex_grid_Y);
	}
		private int Y_grid_offset_calc (int X_remain, int Y_remain, int fullhex_X, int quarthex)		{
		int Y_offset;
		if (fullhex_X % 2 == 0)		{//even
			Y_offset = resolve_botL_topR(X_remain, Y_remain, quarthex);
		} else	{
			Y_offset = resolve_topL_BotR (X_remain, Y_remain);
		if (Y_offset == 0)	{//because the method returns the wrong offset but only at this point
		    Y_offset = -1;
		} else	{
			Y_offset = 0;
		}
		}
	    return Y_offset;
	}
	private int X_grid_offset_calc (int X_remain, int Y_remain, int halfhex_Y, int fullhex_X, int quarthex)	{
	    int X_offset;
	    if (fullhex_X % 2 == 0)	{//even
		if (halfhex_Y % 2 == 0)	{
		    X_offset = resolve_botL_topR(X_remain, Y_remain, quarthex);
		} else	{
		    X_offset = resolve_topL_BotR (X_remain, Y_remain);
		}
	    } else	{//uneven
		if (halfhex_Y % 2 ==0) {
		    X_offset = resolve_topL_BotR (X_remain, Y_remain);

		} else	{
		    X_offset = resolve_botL_topR(X_remain, Y_remain, quarthex);

		}
	    }
	    return X_offset;
	}
	private int resolve_topL_BotR(int X_remain, int Y_remain)	{
	    int offset;
	    if (X_remain > Y_remain /2 )	{//if it's on the right
			offset = 0;
	    } else	{
			offset = -1;
	    }
	    return offset;
	}
	private int resolve_botL_topR (int X_remain, int Y_remain, int quarthex)	{
	    int offset;
	    if (quarthex - X_remain > Y_remain /2)	{//if it's on the left
		offset = -1;
	    } else	{
		offset = 0;
	    }
	    return offset;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
	    return true;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
	    return false;
	}
	@Override
	public boolean mouseMoved(int screenX, int screenY) {//TODO dubplicate code
	    //to make room for the button bar
	    int scr_height = Gdx.graphics.getHeight();
	    int scr_width = Gdx.graphics.getWidth();
	    int hexes_per_column = (scr_height - Assets.halfhex) / (Assets.hex_size);
	    int hexes_per_row = scr_width / (Assets.halfhex+Assets.quarthex);
		    
	    //to filter out negative numbers
	    if (screenX < 0 || screenY-30 < 0){
		return true;
	    }
		    
	    Pair clicked_hex = resolve_hex(screenX,screenY);
	    int clicked_prov_id;
		    
	    //dumps coords if it's outside of the grid
	    if ((int)clicked_hex.getFirst()<0||(int)clicked_hex.getSecond()<0||
		(int)clicked_hex.getFirst()>=hexes_per_row-1||(int)clicked_hex.getSecond()>=hexes_per_column-1){

		clicked_prov_id = -1;
	    }else {
		clicked_prov_id = Assets.resolve_prov_id(clicked_hex);
	    }
		    
		    
	    mouse_move_handeler(clicked_prov_id, screenX, screenY);
	    return true;
	}
	    
	@Override
	public boolean scrolled(int amount) {
	    return false;
	}
    }
    
    private void mouse_move_handeler(int clicked_prov_id, int screenX, int screenY) {
	if (clicked_prov_id != -1){
	    UI.prov_info.setVisible(true);
	    UI.set_prov_tooltip(clicked_prov_id,screenX,screenY);
	}else {
	    UI.prov_info.setVisible(false);
			}
			}

			private void mouse_click_handeler(int clicked_prov_id, int screen_x, int screen_y){
				if (clicked_prov_id != -1){
					if (clicked_prov_id!=Game.selected_prov
							&&Game.Data.who_owns(clicked_prov_id)!=Game.active_player
							&&Game.Data.is_connected(clicked_prov_id,Game.selected_prov)){//if attack is possible
						UI.call_confirm_attack_window(Game.selected_prov, clicked_prov_id);
				}
				if (Game.Data.who_owns(clicked_prov_id)== Game.active_player){
					Game.selected_prov = clicked_prov_id;
					System.out.print("the selected prov is "+Assets.get_prov_name(Game.selected_prov)+"\n");
				}


				//for debugging
				//System.out.print("selected prov: "+Game.selected_prov +"\n");
				//System.out.print("active player: "+Game.active_player+"\n\n");
			}

				//for debuging
			/*		System.out.print("prov id: "+Assets.prov_lookup[hex_grid_X][hex_grid_Y]+"\n");
					System.out.print("x,y: "+hex_grid_X+" "+hex_grid_Y+"\n\n");*/

			}
		}
