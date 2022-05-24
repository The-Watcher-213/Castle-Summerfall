package CastleSummerfall;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * @author @Corbanator this is a grouping of methods to randomly generate any
 *         given game object. A room, an interactable, a floor, an enemy, etc.
 *         It loads the options in from json files contained in the file
 *         specified by pathPath
 */
public class Generator {

  // This stores the location of the file which stores the location of all of
  // the other files, which allows for easily loading new files. Important for
  // easily adding mod support and just easily modifying the file structure.
  private static String pathPath =
      "../data/config/paths.json"; // TODO: add .. to this for
                                   // the binaries
  // @yomas000

  // stores the boss being generated by the generator when it's generating a
  // floor.
  private static NPC boss;

  // depricated. Still here because we were transferring these over to the new
  // format.
  private static String[] roomDescriptions = {
      "You look around and see nothing the room is too dark to see much. But you can see that the walls are gray brick that has moss and water dripping from the old stones.",
      "The room is a massive room with wooden beams sweaping up into high arched ceilings. It has bright chandeliers glowing with hundreds of candles.\nThe wood looks dark and varneshed. It reminds you of viking archetecture.",
      "The room you walked into is dark and dank. It smells like mildew and has slime covering the floor and walls.",
      "You look around and see that this room looks like a storage room. There is crates and barrels of goods waiting for something to happen to them.",
      "The room is what appears to be a bedchamber. There is a massive four poster bed with red velvet curtains.\nYou don't know who would want to sleep in a dungeon though.",
      "The room you walked into has dark stone tiled floors. Rows of pews extend as far as you can see into the scatily lit room. It looks like an old cathedral.",
      "The room you came to is a black stone room with a huge bonfire at one end. There were thick wooden tables on either end that held instrumetns of some dark profession.",
      "You look around the room you came to and see that it looks like a study. It has a light thin wooden desk with a chair behind it. Bookshelves line the room. You look at one of the book titles, it reads, The Study of the Nature of Artificing.",
      "The room you walked into has rows and rows of barrles. looking into one of the barrels you see that it holds a dark red liquid. It smells like wine.",
      "The room you find yourself in now has a counter along one end with lit stoves and ovens. Prepped food lies on the counter, it looks like someone had to leave their job in a rush. The food smells delecious.",
      "This room is throne room. It has large sweeping walls. With massive domed celing. And to cap it all is a huge golden throne with inlade diamonds sparkling in the bright room. You wonder who would sit on such an ostentasions throne.",
      "This room is obviously a jail, it has brushed steel doors with small holes in the bottom and top. Torches iluminate the hallway but enough to see inside the cells.",
      "You look around and find yourself in a childrens nursery. Toys litter the floor and bright murals cover the walls. There is even two small beds in the room.\nThis is getting weird you think to yourself.",
      "The room you walked into is brick. Just brick. Floor, celing, walls: all brick.\nWhat is going on in this place, you wonder.",
      "The room you walked into feels sterile. It has whitewashed walls with steel tables in the center.\nSmaller tables hold all sorts of deadly looking tools.",
      "The room is a hotel you realise. Torch sconces line the very expensive mahogonay hallway.\nThe doors have brass name plates numbering 1 through 25"};

  /**
   * This will generate a floor based on the size desired.
   *
   * @param xSize
   * @param ySize
   * @return Floor
   */
  public static Floor generateFloor(int xSize, int ySize) {

    Random rand = new Random();
    ArrayList<ArrayList<Room>> rooms = new ArrayList<>();

    // choose where the boss will be.
    int xBoss = rand.nextInt(xSize);
    int yBoss = rand.nextInt(ySize);

    // nested for loops to populate rows and columns/
    for (int i = 0; i < xSize; i++) {
      ArrayList<Room> column = new ArrayList<>();
      for (int j = 0; j < ySize; j++) {
        boolean southDoor = false;
        boolean eastDoor = false;
        if (j != 0) {
          southDoor = true;
        }
        if (i != xSize - 1) {
          eastDoor = true;
        }
        // first of two portions making a particular room the boss room.
        boolean hasBoss = false;
        if (i == xBoss && j == yBoss) {
          hasBoss = true;
        }
        Room toAdd = generateRoom(southDoor, eastDoor, hasBoss);

        // second portion making that room the boss room
        if (hasBoss) {
          toAdd.makeStairs();
        }
        column.add(toAdd);
      }
      rooms.add(column);
    }

    Floor result = new Floor(rooms);

    // choose how many enemies to add to the floor.
    int enemyFactor = (xSize * ySize) / 4;
    int enemyCount = rand.nextInt(enemyFactor) + enemyFactor;

    // add the enemies
    for (int i = 0; i < enemyCount; i++) {
      int x = rand.nextInt(xSize);
      int y = rand.nextInt(ySize);
      if (!(x == xBoss && y == yBoss)) {
        result.addNPC(generateEnemy(x, y, 0));
      }
    }

    // if there was a boss generated, set the boss' coordinates and then add it
    if (!Objects.isNull(boss)) {
      boss.setXCoord(xBoss);
      boss.setYCoord(yBoss);
      result.addNPC(boss);
    }

    return result;
  }

  /**
   * This will generate a random room
   *
   * @param interactableMin
   * @param interactableMax
   * @param southDoor
   * @param eastDoor
   * @return Room
   */
  public static Room generateRoom(int interactableMin, int interactableMax,
                                  boolean southDoor, boolean eastDoor) {
    int range = interactableMax - interactableMin;
    Random rand = new Random();
    int loopCount = rand.nextInt(range) + interactableMin;
    ArrayList<Interactable> roomInventory = new ArrayList<Interactable>();
    Door door1;
    Door door2;
    for (int i = 0; i < loopCount; i++) {
      roomInventory.add(generateInteractable());
    }
    if (southDoor) {
      door1 = new Door(true, false, false);
    } else {
      door1 = null;
    }
    if (eastDoor) {
      door2 = new Door(true, false, false);
    } else {
      door2 = null;
    }
    Room result = new Room(
        roomInventory, null,
        roomDescriptions[rand.nextInt(roomDescriptions.length)], door1, door2);
    return result;
  }

  /**
   * This will generate a random Interactable to go in a room
   *
   * @param containerWeight what percentage of interactables should be
   *     containers
   * @return Interactable
   */
  // below this is all the interactable generation.
  public static Interactable generateInteractable(double containerWeight) {
    Random rand = new Random();
    Interactable result;
    double randNum = rand.nextDouble();

    try {
      // read in the filepaths
      File pathFile = new File(pathPath);
      FileReader pathReader = new FileReader(pathFile);
      int i;
      String pathString = "";
      while ((i = pathReader.read()) != -1) {
        pathString += (char)i;
      }

      // almost the same code twice, but one is for containers and the other for
      // normal interactables
      if (randNum < containerWeight) {
        // get the filepath to the containers
        String[] containerPaths =
            Parser.trimQuotes(Parser.parseArray("containers", pathString));
        ArrayList<ContainerPreset> containers = new ArrayList<>();

        // for each file, add the string for each container to the array of all
        // containers
        for (String path : containerPaths) {
          File file = new File(path);
          FileReader reader = new FileReader(file);
          int j;
          String containerString = "";
          while ((j = reader.read()) != -1) {
            containerString += (char)j;
          }
          String[] containerStrings =
              Parser.parseArray("containers", containerString);

          for (String string : containerStrings) {
            containers.add(
                (ContainerPreset)PresetLoader.loadInteractablePreset(string));
          }
        }

        // add up the rarity of all items, then get a random number between 0
        // and that sum. this gets a random item, but weights it toward items
        // with higher rarities
        int totalWeight = 0;
        for (InteractablePreset preset : containers) {
          totalWeight += preset.rarity;
        }
        int choice = rand.nextInt(totalWeight + 1);

        // actually select the particular item
        for (InteractablePreset preset : containers) {
          choice -= preset.rarity;
          if (choice <= 0) {
            result = spinInteractable(preset);
            return result;
          }
        }
        return null;

        // This entire portion of code repeats the previous portion but for
        // normal interactables instead of containers.
      } else {
        String[] interactablePaths =
            Parser.trimQuotes(Parser.parseArray("interactables", pathString));
        ArrayList<InteractablePreset> interactables = new ArrayList<>();

        for (String path : interactablePaths) {
          File file = new File(path);
          FileReader reader = new FileReader(file);
          int j;
          String interactableString = "";
          while ((j = reader.read()) != -1) {
            interactableString += (char)j;
          }
          String[] interactableStrings =
              Parser.parseArray("interactables", interactableString);

          for (String string : interactableStrings) {
            interactables.add(PresetLoader.loadInteractablePreset(string));
          }
        }
        int totalWeight = 0;
        for (InteractablePreset preset : interactables) {
          totalWeight += preset.rarity;
        }
        int choice = rand.nextInt(totalWeight + 1);

        for (InteractablePreset preset : interactables) {
          choice -= preset.rarity;
          if (choice <= 0) {
            result = spinInteractable(preset);
            return result;
          }
        }
        return null;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * takes in a preset, spins it up, then gives it the proper doors and
   * interactables, as well as returning either a boss or not a boos based on
   * the boss parameter.
   *
   * @param preset
   * @param interactableMin
   * @param interactableMax
   * @param southDoor
   * @param eastDoor
   * @param boss
   * @return Room
   */
  public static Room generateRoom(RoomPreset preset, int interactableMin,
                                  int interactableMax, boolean southDoor,
                                  boolean eastDoor, boolean boss) {

    // choose how many interactables to generate in the room.
    int range = interactableMax - interactableMin;
    Random rand = new Random();
    int loopCount;
    if (interactableMax > interactableMin) {
      loopCount = rand.nextInt(range) + interactableMin;
    } else {
      loopCount = interactableMin;
    }

    // add interactables from the preset
    ArrayList<Interactable> roomInventory = new ArrayList<>();
    for (InteractablePreset interactable : preset.interactables) {
      if (!Objects.isNull(spinInteractable(interactable))) {
        roomInventory.add(spinInteractable(interactable));
      }
    }

    // add description interactables from the preset
    ArrayList<Interactable> descriptionInteractables = new ArrayList<>();
    for (InteractablePreset interactable : preset.descriptionInteractables) {
      descriptionInteractables.add(spinInteractable(interactable));
    }

    // spin the room from the preset
    Room result = spinRoom(preset, southDoor, eastDoor);

    // if it's not a boss room, add the room inventory. (Boss rooms don't have
    // interactables in them.)
    if (!boss) {
      for (int i = 0; i < loopCount; i++) {
        result.addItem(generateInteractable());
      }
    }

    return result;
  }

  /**
   * Generates a room with the default values for number of interactables and by
   * loading in all possible presets.
   *
   * @overload
   * @param southDoor
   * @param eastDoor
   * @param boss
   * @return Room
   */
  public static Room generateRoom(boolean southDoor, boolean eastDoor,
                                  boolean boss) { // read all the filepaths
    File filePaths = new File(pathPath);
    String[] files;
    String pathString = "";

    try {
      FileReader pathIn = new FileReader(filePaths);
      int i = 0;

      while ((i = pathIn.read()) != -1) {
        pathString += (char)i;
      }
      pathIn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    // get the paths to the room preset files
    files = Parser.trimQuotes(Parser.parseArray("room-presets", pathString));

    // two arraylists, one for boss rooms, so that it can only make boss rooms
    // when told to.
    ArrayList<RoomPreset> presets = new ArrayList<>();
    ArrayList<RoomPreset> bossPresets = new ArrayList<>();

    // for each file, read what's in it and load all the roomPresets, then add
    // each of them to the appropriate Arraylist.
    for (String file : files) {
      File presetFile = new File(file);
      String presetString = "";
      try {
        FileReader presetIn = new FileReader(presetFile);
        int i = 0;

        while ((i = presetIn.read()) != -1) {
          presetString += (char)i;
        }
        presetIn.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      ArrayList<RoomPreset> toAdd = PresetLoader.loadRoomPresets(presetString);

      for (RoomPreset roomPreset : toAdd) {
        if (Objects.isNull(roomPreset.boss)) {
          presets.add(roomPreset);
        } else {
          bossPresets.add(roomPreset);
        }
      }
    }

    // chooses a roompreset from the appropriate arraylist, then spins it up
    // with the default item sets
    Random rand = new Random();
    int choice;
    Room result;
    if (boss) {
      if (bossPresets.size() > 1) {
        choice = rand.nextInt(bossPresets.size());
        result = generateRoom(bossPresets.get(choice), 0, 0, southDoor,
                              eastDoor, boss);
      } else {
        choice = 0;
        result = generateRoom(bossPresets.get(choice), 0, 0, southDoor,
                              eastDoor, boss);
      }
    } else {
      choice = rand.nextInt(presets.size());
      result =
          generateRoom(presets.get(choice), 1, 3, southDoor, eastDoor, boss);
    }

    return result;
  }

  /**
   * Makes a room based on a preset, but doesn't add any interactables to it.
   *
   * @param preset
   * @param southDoor
   * @param eastDoor
   * @return Room
   */
  public static Room spinRoom(RoomPreset preset, boolean southDoor,
                              boolean eastDoor) {

    Random rand = new Random();

    // add all of the preset's normal interactables to the inventory
    ArrayList<Interactable> interactables = new ArrayList<Interactable>();
    for (InteractablePreset interactable : preset.interactables) {
      interactables.add(spinInteractable(interactable));
    }

    // choose one of the possible descriptions at random
    String description =
        preset.descriptions[rand.nextInt(preset.descriptions.length)];

    // add all of the description interactables to the descriptionInteractables
    ArrayList<Interactable> descriptionInteractables =
        new ArrayList<Interactable>();
    for (InteractablePreset interactable : preset.descriptionInteractables) {
      descriptionInteractables.add(spinInteractable(interactable));
    }

    Door doorSouth;
    Door doorEast;

    // add doors.
    if (southDoor) {
      doorSouth = new Door(true, false, false);
    } else {
      doorSouth = null;
    }

    if (eastDoor) {
      doorEast = new Door(true, false, false);
    } else {
      doorEast = null;
    }

    // add the boss
    if (!Objects.isNull(preset.boss)) {
      boss = preset.boss;
    }

    // actually make the room
    return new Room(interactables, descriptionInteractables, description,
                    doorSouth, doorEast);
  }

  /**
   * This generate a default Interactable with the default weight of 20%
   * container chance
   *
   * @return Interactable
   */
  public static Interactable generateInteractable() {
    return generateInteractable(.2);
  }

  /**
   * Make an actual interactable from a preset
   *
   * @param preset
   * @return Interactable
   */
  public static Interactable spinInteractable(InteractablePreset preset) {
    // return null if the preset is null, to avoid errors
    if (Objects.isNull(preset)) {
      return null;
    }

    Random rand = new Random();
    String name = preset.name;

    // don't return anything unless it at least has a description
    if (Objects.isNull(preset.descriptions)) {
      return null;
    }

    // choose a random description
    String description =
        preset.descriptions[rand.nextInt(preset.descriptions.length)];

    // add abilities, for future extensibility
    ArrayList<Ability> abilities = new ArrayList<>();

    // add all of the abilities from the abilityOptions
    ArrayList<InteractablePreset.AbilityOption> options = new ArrayList<>();
    for (InteractablePreset.AbilityOption abilityOption :
         preset.abilityOptions) {
      options.add(abilityOption);
    }

    for (InteractablePreset.AbilityOption abilityOption : options) {
      for (int i = 0; i < abilityOption.number; i++) {
        int choice = rand.nextInt(abilityOption.options.size());
        abilities.add(abilityOption.options.get(choice));
        abilityOption.options.remove(choice);
      }
    }

    // if it's a container or weapon, do additional stuff.
    if (preset instanceof ContainerPreset) {
      return spinContainer((ContainerPreset)preset);
    }
    if (preset instanceof WeaponPreset) {
      return spinWeapon((WeaponPreset)preset);
    }
    return new Interactable(name, description, preset.size, preset.weight,
                            preset.canBePickedUp, abilities);
  }

  /**
   * Makes a container based on a containerPreset
   *
   * @param preset
   * @return Container
   */
  public static Container spinContainer(ContainerPreset preset) {

    Random rand = new Random();
    String name = preset.name;

    // choose a random description
    String description =
        preset.descriptions[rand.nextInt(preset.descriptions.length)];

    // add an inventory
    ArrayList<Interactable> inventory = new ArrayList<>();
    int loopCount =
        rand.nextInt(preset.maxItems - preset.minItems) + preset.minItems;
    for (int i = 0; i < loopCount; i++) {
      inventory.add(generateInteractable());
    }

    return new Container(name, description, preset.size, preset.weight,
                         preset.canBePickedUp, inventory, preset.inventorySize);
  }

  /**
   * Return a weapon based on a weaponPreset
   *
   * @param preset
   * @return Weapon
   */
  public static Weapon spinWeapon(WeaponPreset preset) {
    Random rand = new Random();
    String description =
        preset.descriptions[rand.nextInt(preset.descriptions.length)];
    int pierce = 0;

    // each section that looks like this is choosing either a single value or a
    // value from a range, depending on if there's a range.
    if (preset.pierceRange != 0) {
      pierce = rand.nextInt(preset.pierceRange) + preset.pierce;
    } else {
      pierce = preset.pierce;
    }

    // minimum damage
    int damage = 0;
    if (preset.damageRange != 0) {
      damage = rand.nextInt(preset.damageRange) + preset.damage;
    } else {
      damage = preset.damage;
    }

    // range in which damage can be
    int range = 0;
    if (!(preset.rangeRange <= 0)) {
      range = rand.nextInt(preset.rangeRange) + preset.range;
    } else {
      range = preset.range;
    }

    return new Weapon(preset.size, preset.weight, preset.canBePickedUp,
                      preset.name, description, pierce, damage, range);
  }

  /**
   * Make an NPC from a preset at the coordinates given.
   *
   * @param xCoord
   * @param yCoord
   * @param preset
   * @param challengeRating
   * @return NPC
   */

  public static NPC spinNPC(int xCoord, int yCoord, NPCPreset preset,
                            int challengeRating) {
    // return null if the preset is null. Avoids errors
    if (Objects.isNull(preset)) {
      return null;
    }

    Random rand = new Random();
    NPCAlliance npcAlliance = preset.npcAlliance;

    // choose a random description
    String description =
        preset.descriptions[rand.nextInt(preset.descriptions.length)];

    // choose the stats from the possible ranges for each
    int AC = randomFromRange(preset.ACRange);
    int strength = randomFromRange(preset.strRange);
    int dexterity = randomFromRange(preset.dexRange);
    int constitution = randomFromRange(preset.conRange);
    int intelligence = randomFromRange(preset.intRange);
    int wisdom = randomFromRange(preset.wisRange);
    int charisma = randomFromRange(preset.chaRange);
    int noise = randomFromRange(preset.noiseRange);
    int shield = randomFromRange(preset.shieldRange);

    // choose a random name
    String name = preset.name[rand.nextInt(preset.name.length)];

    NPC result = new NPC(xCoord, yCoord, AC, strength, dexterity, constitution,
                         intelligence, wisdom, charisma, noise, shield, name,
                         npcAlliance, description);

    // Load the NPC's inventory
    for (InteractablePreset itemPreset : preset.inventory) {
      result.addInventory(spinInteractable(itemPreset));
    }

    return result;
  }

  /**
   * return a random number from a range, given by a length 2 integer array
   * index 0 is the min and index 1 is the max
   *
   * @param range
   * @return int
   */
  private static int randomFromRange(int[] range) {
    Random rand = new Random();
    if (range[1] > range[0]) {
      return rand.nextInt(range[1] - range[0]) + range[0];
    } else {
      return range[0];
    }
  }

  /**
   * generates a random enemy by choosing a preset from the files and puts it in
   * the proper coordinates.
   *
   * @param xCoord
   * @param yCoord
   * @param challenge
   * @return NPC
   */
  private static NPC generateEnemy(int xCoord, int yCoord, int challenge) {

    Random rand = new Random();
    File filePaths = new File(pathPath);
    String[] files;
    String pathString = "";

    // read in where the files are
    try {
      FileReader pathIn = new FileReader(filePaths);
      int i = 0;

      while ((i = pathIn.read()) != -1) {
        pathString += (char)i;
      }
      pathIn.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    files = Parser.trimQuotes(Parser.parseArray("enemy-presets", pathString));

    // for each file, load in all the enemy presets. add them all to a single
    // arraylist
    try {
      ArrayList<String> enemyChoices = new ArrayList<>();
      for (String fileString : files) {
        File file = new File(fileString);
        FileReader reader = new FileReader(file);
        String string = "";

        int i = 0;
        while ((i = reader.read()) != -1) {
          string += (char)i;
        }

        String[] stringsToAdd = Parser.parseArray("enemy-presets", string);
        for (String stringToAdd : stringsToAdd) {
          enemyChoices.add(stringToAdd);
        }
      }

      // choose one of the enemypresets, make it into an enemy, then return it.
      String choice = enemyChoices.get(rand.nextInt(enemyChoices.size()));
      return spinNPC(xCoord, yCoord, PresetLoader.loadNpcPreset(choice), 0);

    } catch (Exception e) {
      e.printStackTrace();
    }
    // this should never happen.
    return null;
  }
}
