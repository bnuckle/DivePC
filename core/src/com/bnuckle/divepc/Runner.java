/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bnuckle.divepc;

import com.badlogic.gdx.*;
import com.bnuckle.divepc.pc.ZHL16;

public class Runner extends Game
{

	ZHL16 pc;

	@Override
	public void create () {
		pc = new ZHL16();
		pc.goToDepth(100);
		pc.printCompartments();
		setScreen(new Screen(){
			@Override
			public void show()
			{

			}

			@Override
			public void render(float delta)
			{
				pc.step(delta);

				if(Gdx.input.isKeyJustPressed(Input.Keys.A))
				{
					pc.printCompartments();
				}
			}

			@Override
			public void resize(int width, int height)
			{

			}

			@Override
			public void pause()
			{

			}

			@Override
			public void resume()
			{

			}

			@Override
			public void hide()
			{

			}

			@Override
			public void dispose()
			{

			}
		});
	}

	@Override
	public void render () {
		super.render();
	}



}
