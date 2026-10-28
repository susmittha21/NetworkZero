# NETWORK ZERO: The Last Lab Session

Java 17 / LibGDX desktop game. From the project root, run:

```powershell
gradle desktop:run
```

## Room 1

Choose **New Game / Play Solo** to watch the story and enter Room 1. **Continue** appears after a room save is created and resumes `saves/room1_state.json`. Pause with the top bar or Esc; **Save and Exit** writes the current puzzle state. **Settings / Skip story (testing)** enters Room 1 directly.

Room artwork uses the supplied files in `assets/labs/`: `bg_flat.png`, `bg_online.png`, and `exit_hallway.png`. The room has no decorative animation; only the brief lighting cross-fade, toast fade, and terminal cursor blink are used. Audio is optional and missing clips are skipped with one log line each.

## Editing room data

- `assets/data/room1.json` defines room image paths, dimensions, and normalized hotspot rectangles (`x`, `y`, `w`, `h`). Coordinates use the source image's top-left origin and fractions from 0 to 1.
- `assets/data/puzzles.json` defines wire order, patch links, topology answer, and door code.
- `assets/data/hints.json` contains three hint tiers for each puzzle.
- **F1** shows hotspot ids and source-image coordinates. **F2** reloads room, puzzle, and hint JSON. **F3** marks the open puzzle solved for debugging. **F4** enables the hotspot editor: click and drag a hotspot, use arrow keys to nudge by one source pixel, and **Ctrl+S** to save `room1.json`.

Progress, settings, and SQLite hint history are stored under `~/.networkzero/` (or the selected profile subfolder).

## LAN co-op

Choose **New Game > Host Co-op (LAN)** on one computer. The host displays its LAN address, for example `10.10.123.206:54555`. On the second computer choose **Join Co-op (LAN)**, select a discovered host or type either the IP address alone or the complete `IP:port` shown by the host, then connect and ready up. Both computers must be on the same LAN; allow Java through the firewall on **Private networks**. Both players must be ready before the host starts the story. The host is authoritative; puzzle actions and shared state are synchronized through KryoNet. Chat is available from the Room 1 bottom bar.

Allow Java through the firewall on **Private networks**. If prompted for manual rules, allow TCP `54555` and UDP `54777`.

### Test two instances on one computer

Run the regular host in one terminal:

```powershell
gradle desktop:run
```

Then start the isolated client in a second terminal:

```powershell
gradle desktop:runClient
```

The client opens with the `client` profile and a distinct window title. Join `127.0.0.1`. Profile data is kept in `~/.networkzero/` for the regular instance and `~/.networkzero/client/` for the client, so saves and settings remain separate.

Run the unit and loopback multiplayer tests with:

```powershell
gradle test
```

`assets/data/coop.json` controls whether the host sees the logbook and the client sees the notice board. Set `asymmetricClues` to `false` to allow both players to open both clue panels.

Press Start 2P is bundled at `assets/fonts/PressStart2P.ttf` under the SIL Open Font License; its license is in `assets/fonts/PressStart2P-OFL.txt`.

## Room 3: Network Control Office

The game has two rooms: Room 1 (Networking Laboratory) followed by Room 3 (Network Control Office), which is the final room. There is no Room 2. Room 1 completion records `room: 3`; Continue then resumes `saves/room3_state.json`. Finishing Room 3 records `room: "complete"` and disables Continue. Room 3 starts with a fresh 45 minute timer and ends at the Escape Complete screen.

Room 3 uses the supplied `assets/controls/control_bg.png` and optional `assets/controls/control_online_bg.png`. The background is drawn in the fixed 1680×945 room view. Hotspot rectangles in `assets/data/room3.json` are normalized fractions from the source image's top-left; overlapping rectangles select the smallest area. The crop viewer converts those same source coordinates to the texture's bottom-left origin and keeps the original crop aspect ratio at no more than 2× scale.

Edit the cabinet answer, attacker IP, distinct port count, service mappings, camera times, and exit code in `assets/data/puzzles3.json`. Edit the three hint tiers in `assets/data/hints3.json`. The F1 overlay shows hotspot ids and source coordinates; F2 reloads the Room 3 definition and puzzle data; F3 assists with the currently open puzzle; F4 enables hotspot move/edit mode and Ctrl+S saves the JSON; F5 shows network diagnostics. Co-op clues are assigned by role in `assets/data/room3.json` and enabled by `assets/data/coop.json`.

Run the logic and LAN loopback tests with `gradle test`. They cover the Room 3 puzzle rules and a complete two-peer solve through the host-authoritative intent protocol.

## Ending Story Sequence

After successfully escaping Room 3, the game transitions to a cinematic **Ending Story Sequence** before displaying the final **ESCAPE COMPLETE** summary screen.

### Architecture & System Reuse
The ending sequence reuses the core story engine (`StoryScreen`, `StoryDirector`, `TypewriterText`, `CameraMove`, `LetterboxBars`, `SkipHold`, `StoryAudio`) without duplicating screen classes:
- `StoryScreen` accepts a data file path (`data/story.json` for intro, `data/ending.json` for ending) and an `onComplete` callback `Runnable`.
- **Presentation**: 12% cinematic letterbox bars, mipmap linear-linear texture filtering on painted panels, Silkscreen pixel typography (`fonts/Silkscreen-Regular.ttf` & `fonts/Silkscreen-Bold.ttf`), orange-outlined dialogue caption box with ~35 chars/sec typewriter typing, and 0.8s cross-dissolves through black between panels.
- **Counter layout**: The ending places the panel counter (e.g., `1/4`) in the top-right corner; the intro places it in the top-left corner alongside the `SKIP INTRO` button.
- **Title card**: A code-drawn closing card featuring the heading `"NETWORK RESTORED."`, subheading `"THE LAST LAB SESSION IS OVER."`, and a pulsing `"PRESS SPACE TO CONTINUE"` prompt after 3 seconds.
- **Fail-safe fallback**: If any ending panel image or font is missing, `StoryScreen` logs an error specifying the missing file and skips directly to the `ESCAPE COMPLETE` screen. Missing audio clips are skipped silently with a single log line.

### Triggering the Ending
- **Solo Escape**: Entering the correct 4-digit code (`6742`) on the Room 3 exit keypad displays the "Door unlocked" toast, writes `room: "complete"` to `saves/progress.json`, fades Room 3 to black over 0.5s, stops the alarm, and launches the ending sequence.
- **LAN Co-op**: When the host validates the exit keypad submission, KryoNet broadcasts the `EndingStart` game event. Both players fade into the ending sequence with independent pacing and skip controls, synchronizing at the final `ESCAPE COMPLETE` screen.
- **Watch Ending Again (Solo)**: The `ESCAPE COMPLETE` screen features a "WATCH ENDING AGAIN" button (solo only) to replay the ending without resetting game progress.
- **Landing Settings**: The Settings dialog contains a "REPLAY ENDING" button next to "REPLAY STORY", returning directly to Settings when finished.
- **Dev Room Select (F9)**: Pressing **F9** on the Landing Screen opens the developer room-select modal with options for `NETWORKING LABORATORY`, `NETWORK CONTROL OFFICE`, and `ENDING`.

### Editing `assets/data/ending.json`
`assets/data/ending.json` configures the ending panels, camera motion, audio cues, and title card:
```json
{
  "sourceWidth": 1920,
  "sourceHeight": 1080,
  "letterboxRatio": 0.12,
  "filmGrain": true,
  "vignette": true,
  "panels": [
    {
      "id": "end1",
      "image": "story/end_panel_1.png",
      "text": [
        "THE EXIT DOOR RELEASES.",
        "FOR THE FIRST TIME TONIGHT, THE LOCK DOES NOT HOLD."
      ],
      "camera": {
        "startZoom": 1.00,
        "endZoom": 1.10,
        "startPan": [0, 0],
        "endPan": [60, -10],
        "duration": 14
      },
      "audioCues": [
        { "line": 0, "sound": "audio/door_open.ogg" }
      ]
    }
  ],
  "titleCard": {
    "text": [
      "NETWORK RESTORED.",
      "THE LAST LAB SESSION IS OVER."
    ],
    "prompt": "PRESS SPACE TO CONTINUE"
  }
}
```

- **Panels**:
  - `id`: Unique panel identifier (`end1`, `end2`, `end3`, `end5`).
  - `image`: Relative path to panel image (e.g. `story/end_panel_1.png`).
  - `text`: Array of narration lines revealed line-by-line via typewriter.
  - `camera`: Ken Burns effect parameters: `startZoom` and `endZoom` (clamped to 1.00–1.12), `startPan` [x, y], `endPan` [x, y], and `duration` in seconds.
  - `audioCues`: Synchronized sound events: `{ "line": <lineIndex>, "sound": "<audioPath>" }`. Plays `ending_music.ogg` (looping with fade-in and fade-out), `door_open.ogg` (on end1 line 0), and `power_up.ogg` (on end2 line 1).

### Story & Ending Controls
- **Space / Click**: Advance typewriter text or transition to the next panel.
- **Hold Esc (1s)**: Fill the circular skip meter to skip the ending and proceed to `ESCAPE COMPLETE`.
- **F1**: Toggle debug overlay with live normalized mouse position.
- **F2**: Live-reload `assets/data/ending.json` from disk and restart the current panel without restarting the game.
- **F3**: Instantly skip to the next panel.

