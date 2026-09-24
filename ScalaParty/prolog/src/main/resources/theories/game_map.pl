% Game map theory: generates the layout of the arena for a given number of players.

arena(800, 800).
max_players(4).
spawn_radius(280).
first_spawn_angle(225).

game_map(Players, game_map(arena(Width, Height), Spawns)) :-
    max_players(Max),
    between(1, Max, Players),
    arena(Width, Height),
    LastIndex is Players - 1,
    findall(Spawn, (between(0, LastIndex, Index), player_spawn(Index, Players, Spawn)), Spawns).

player_spawn(Index, Players, spawn(X, Y, Heading)) :-
    first_spawn_angle(FirstAngle),
    Angle is FirstAngle + Index * 360.0 / Players,
    radians(Angle, Radians),
    arena(Width, Height),
    spawn_radius(Radius),
    X is round(Width / 2.0 + Radius * cos(Radians)),
    Y is round(Height / 2.0 + Radius * sin(Radians)),
    Heading is Angle - 180.

between(Low, High, Low) :- Low =< High.
between(Low, High, X) :- Low < High, Next is Low + 1, between(Next, High, X).

radians(Degrees, Radians) :- Radians is Degrees * 4 * atan(1) / 180.
