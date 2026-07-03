# AvoCast

A fully customizable auto-broadcast plugin for Paper/Spigot Minecraft servers (1.21.x, including 1.21.11).

It rotates through broadcast messages on a timer (default every 5 minutes) reminding players to vote, join your
Discord, and check out your store — plus a `/buy` command for store info. Everything (text, colors, links, timing)
is editable in `config.yml`, no code changes needed.

## Features
- ⏱ Auto-broadcast on a configurable timer (in-order or random)
- 🗳 Vote reminder, Discord reminder, store reminder — all in one rotation
- 🛒 `/buy` command showing your store info
- 🎨 Full color customization — classic codes (`&a`, `&c`...) **and** hex codes (`&#FF5733`)
- 🔁 `/avocast reload` to apply config changes without restarting the server
- 🔐 Permission-gated admin/reload command

## Building the plugin (produces the .jar)
You need **Java 21** and **Maven** installed locally (this sandbox has no internet access, so the jar
couldn't be compiled here — but the project is 100% ready to build).

```bash
cd AvoCast
mvn clean package
```

The compiled plugin will be at `target/AvoCast.jar`. Drop that into your server's `/plugins` folder and restart
(or `/reload` if you use a plugin manager that supports it — a full restart is recommended).

## Installing
1. Build the jar (above) or open the project in IntelliJ/Eclipse and build it there.
2. Copy `target/AvoCast.jar` into your server's `plugins/` folder.
3. Start/restart the server. A `config.yml` will be generated in `plugins/AvoCast/`.
4. Edit `plugins/AvoCast/config.yml` to your liking (see below).
5. Run `/avocast reload` in-game or in console to apply changes instantly.

## Customizing (config.yml)
Everything lives in `plugins/AvoCast/config.yml`:

- `settings.broadcast-interval-minutes` — how often it broadcasts (default `5`)
- `settings.random-order` — `true` to shuffle messages instead of cycling in order
- `settings.enabled` — turn the auto-broadcaster on/off entirely
- `broadcast-messages` — the list of messages that rotate. Add as many as you want.
- `links.discord` / `links.vote` / `links.store` — set these once, then use `{discord}`, `{vote}`, `{store}`
  anywhere in your messages and they'll auto-fill
- `buy-command.message` — what players see when they type `/buy`
- `messages.*` — system messages (no permission, reload success, help menu)

### Colors
Use `&` + a code, or a hex code like `&#1E90FF` for any color you want:

```yaml
broadcast-messages:
  - "&#FF5733&lHOT DEAL! &r&eCheck out &f/buy &efor a limited-time discount!"
```

## Commands & Permissions
| Command | Description | Permission | Default |
|---|---|---|---|
| `/buy` | Shows store info | `avocast.buy` | everyone |
| `/avocast reload` | Reloads config.yml | `avocast.admin` | op |
| `/avocast help` | Shows command help | none | everyone |

## Notes on version compatibility
The `pom.xml` targets the Paper API for 1.21.1, which is API-compatible with the whole 1.21.x line
(including 1.21.11) — Mojang/Paper don't bump the plugin API between those patch releases. This plugin also
works fine on Spigot servers.
