# TODO

## Cutting Algorithm Changes

## Knot Finding

## Unit Testing

1. - [x] Add Button to generate manifold tests and solutions in new unit test file (VK_G)

2. - [x] Should generate one test file per cut segment pair

3. - [x] Should also generate one master manifold file that contains all of the cut segment pairs

## UI Tools General

1. - [x] Abstract Tool should have some general info that it displays for each tool including fps and name

2. - [x] Should keep track of the current selected tool and not allow two tools to be active at once, all tools should only effect the ui's state once the final necessary selection is made

3. - [x] Need to have one side of the knotPoint/cutPoint pair follow the cursor instead of just defaulting to clockwise knotPoint.

4. - [x] Figure out screen-space to point-space conversion

5. - [x] Figure out Skewed bounding boxes around Line Segments

6. - [x] Should set a segment as the "hover segment" when within some distance to one of the line Segments in the current Manifold Knot.

7. - [x] Should expand the Segment Bounding boxes to work at any knot level not just the manifold knot.

8. - [x] Should only have one tool active at a time and call the super class's draw and click functions

9. - [x] Clicking and dragging should change the panX, panY to where the new mouse position is.

10. - [x] Pressing Up/Down Arrow Key should change the Knot Level unless blocked.

11. - [x] Pressing VK_R should reset the camera and reset the active tool but not exit it.

12. - [x] Pressing and Holding Left/Right should repeat the action at some reasonable rate.

13. - [x] Need to find some way to determine clockwise versus anti clockwise.

14. - [x] Pressing VK_Enter should advance the tool if there is a selection active

## UI Free Tool

1. - [x] Clicking on a point should mark it with a circle around the point and change the perma-hover to the selected VirtualPoint

2. - [x] Hover over a point should display information about the VirtualPoint like, it's closest three segments and their distances, the x/y coordinates and the minimum knot the point is in.

3. - [x] Info Panel should display the point's containing Knot.

4. - [x] Clicking on the knot text should center the camera on the Knot.

5. - [x] Pressing Left/Right Arrow Key should move the selected point to the next clockwise point in the current knot level.

6. - [x] Knot's should display their closest two segments with rotating dashed lines. (use dash phase).

7. - [x] Info Panel should the full knot structure with different colors for the different knots being represented by their metro diagram colors

8. - [x] hovering over the knot text should switch the metro draw index to display that knot and highlight it somehow

## UI CutMatch Tool

1. - [x] Make tool to test out new cut match groups.

2. - [x] Should follow pointer with a cyan line and display a yellow line on the nearest cut segment to the pointer.

## UI Manifold Find Tool

1. - [x] CTRL + VK_F in manifold mode should start a two step process where you click to select two knot points in the manifold.

2. - [x] After the second cut segment is selected search the manifold list for a manifold where the two cut segment's exist and update the manifold index to this manifold.

3. - [x] Pressing Left/Right Arrow Key should move the hover/selected knotPoint(selected if isn't null) to the next clockwise knotPoint in the list of manifolds.

## UI Compare Route Map Tool

Idea behind tool is that we would like to compare two pairs of cut segment shortest path information where the pair have the same starting cut segment but differing end cut segments.

1. - [x] A matching channel should have the same shortest route delta back to the start as its comparison peer

2. - [x] Color all half-segments blue who match entirely on the currently selected route view (Disconnected, Connected)

3. - [x] Color all non-matching half-segments red

4. - [x] Color all flipped half-segments yellow (the connected and disconnected channels are flipped)

5. - [x] Clicking on a half-segment/vp should display in the Info Tab the route information.

6. - [x] When a half-segment is selected we should see its route of cutmatches leading back to the start.

7. - [x] Increasing or decreasing level keys: '[' and ']' should change the route view.

8. - [x] Tool should have two states Find and Compare

9. - [x] Compare state is the novel pieces of this description and can only be started after the Find state.

10. - [x] Find state is the starting state and is similar to the find manifold tool except we need to find 3 segments

11. - [x] When in compare mode we should still show the the cutmatches on hover but in comparison, matching cuts and matches will still be in cyan and orange, non-matching cuts from the alpha manifold will be in purple and green and non-matching cuts from the beta manifold will be in magenta and yellow

## UI Message Panel

## UI Info Panel

1. - [x] Panel should be scrollable.

## UI

1. - [x] Display the current tool mode on the right side panel

2. - [x] Make it so Zooming in and out is always centered on the middle of the screen

3. - [x] VK_ESCAPE should return the ui to it's default display state and exit any active tool, resetting the tool.

4. - [x] VK_B should cycle the manifold index

5. - [x] Should we only draw one line segment per segment? i.e implement some kind of Z-Buffer? currently just works on the order of drawing, but could imagine storing two colors for every segment and draw each segment as a gradient. As well as storing null color? or a list of segments to draw.

6. - [x] Need to be able to distinguish between Ctrl + Key and Key with precedence for Ctrl + Key.

## Main Menu

1. - [x] Main Menu Item's should include: Continue : last_file_loaded.ix, Load, Puzzle, Map Editor.

## Menu Items

1. - [x] Menu should be scrollable within the menu's bounds.

2. - [x] Menu items should be elongated hexagons, see ratchet 2.

3. - [x] Menu Items should flash on hover

## 2D Camera

1. - [x] Need to tie the camera zooming and move speed to the framerate, should use the clock class to figure this out

## Tooltip

1. - [x] Should be the width of the text with some reasonable limits

## Shaders

1. - [x] SDF Union between two textures

2. - [x] SDF Lines with dashes, should be able to change between round caps and flat caps, should also be able to animate the phase of the dashes

3. - [x] SDF Texture with separate border color.

4. - [x] SDF Circle with fill or border

5. - [x] Font Atlas from rendered font

## 3D Graphics

1. - [x] Figure out how to do GLSL Shading

2. - [x] Figure out how to load font's based on bitmaps.

3. - [x] Figure out how to load arbitrary shapes as signed distance fields

## UI Knot Surface View Tool

## Key Input

## UI Negative CutMatch View Tool

1. - [x] Make tool to view which KnotPoints have any CutMatches that have a negative weight.

2. - [x] Info Panel should show the current hover cut's length delta as well as name the segments involved

3. - [x] On pressing Ctrl+N should display the top manifold with all half-segments that only have positive weights leading to them in Medium-Green.

4. - [x] Half-Segments that have negative weight cut matches should be colored RED.

5. - [x] When we hover over a RED segment, display all of the cutMatches that lead to that KnotPoint with a negative weight, display the cut Segment in Yellow and the Match Segment in CYAN.

6. - [x] Pressing Left/Right Arrow Key should move the hover knotPoint to the next clockwise knotPoint with negative weight.

7. - [x] Need to make it so you can change the color at the point rather than having only one color per segment.

## Filesystem

1. - [x] When we are in manifold mode update key (VK_U) should update the cut match CUTANS in the respective file.

2. - [x] Should only update when we have the correct answer length and the cutMatch is smaller than the one stored in the file

3. - [x] Manifold files should have the following format djbouti-8-34-manifold_KP1-CP1_KP2-CP2

4. - [x] Should be able to load multiple manifolds per file

## General Speedup

1. - [x] Remove repeated segment pairs from the main loop (2x speedup)

2. - [x] Remove redundant error handling

3. - [x] For less accuracy dependent problems could use heuristic of distance to KnotPoints from externals plus distance between CutPoints as best measure of where to calculate internal structure changing from N^7 to N^3 operation.

## Bugfix

1. - [x] investigate why there is a second to load graphics before the window becomes the right size.
