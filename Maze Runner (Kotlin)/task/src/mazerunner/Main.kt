package mazerunner

import java.awt.Point
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayDeque

class GameMazeRunner() {
    var maze = emptyList<MutableList<Char>>()
    var solutionPath = ArrayDeque<Point>()
    val width: Int
        get() = maze[0].size
    val height: Int
        get() = maze.size
    val entrance = Point(0, 0)

    fun initialize(dim: Int) {
        maze = List(dim) { MutableList(dim) { 'w' } }
        generateMaze()
    }

    fun generateMaze() {
        val frontier = mutableSetOf<Point>()
        val oldFrontier = mutableSetOf<Point>()
        val startX = Random().nextInt(1, width - 1)
        val startY = Random().nextInt(1, height - 1)
        maze[startY][startX] = 'p'
        frontier.addAll(Point(startX, startY).getNeighbors(frontier = true, twoCells = true))

        while (frontier.isNotEmpty()) {
            val randomPoint = frontier.random()
            frontier.remove(randomPoint)
            val neighbors = randomPoint.getNeighbors(twoCells = true)
            if (neighbors.isNotEmpty()) {
                connect(randomPoint, neighbors.random())
            }
            if (neighbors.size < 2)
                if (!oldFrontier.add(randomPoint)) continue
            frontier.addAll(randomPoint.getNeighbors(frontier = true, twoCells = true))
        }
        // set an entrance at the first wall
        entrance.y = maze.indexOfFirst { it[1] == 'p' }
        maze[entrance.y][0] = 'p'

        // set an exit at the last wall randomly
        val exitY = maze.indices.filter { maze[it][width - 2] == 'p' }.random()
        maze[exitY][width - 1] = 'p'
    }

    fun connect(p1: Point, p2: Point) {
        val x = (p1.x + p2.x) / 2
        val y = (p1.y + p2.y) / 2
        maze[y][x] = 'p'

        if (p1.y in 1..height - 2 && p1.x in 1..width - 2)
            maze[p1.y][p1.x] = 'p'
    }

    // Get the neighbors cells - surrounding cells at a distance of 1 and a passage
    // frontier = true: Get the surrounding frontier neighbours - cells with a wall
    // twoCells = true: Get the surrounding cells at a distance of 2
    private fun Point.getNeighbors(frontier: Boolean = false, twoCells: Boolean = false): List<Point> {
        val directions = if (twoCells)
            listOf(Point(2, 0), Point(0, 2), Point(-2, 0), Point(0, -2))
        else
            listOf(Point(1, 0), Point(0, 1), Point(-1, 0), Point(0, -1))

        return directions.map { Point(this.x + it.x, this.y + it.y) }
            .filter { it.x in 0 until width && it.y in 0 until height && (maze[it.y][it.x] == 'p') xor frontier }
    }

    fun solveMaze() {
        entrance.y = maze.indexOfFirst { it[0] == 'p' }
        val visited = mutableSetOf<Point>()
        solutionPath.clear()
        solutionPath.addLast(entrance)
        var node: Point

        loop@ while (true) {
            node = solutionPath.last()
            visited.add(node)
            if (node.x != width - 1) {
                val neighbors = node.getNeighbors()
                for (n in neighbors) {
                    if (n !in visited) {
                        solutionPath.addLast(n)
                        continue@loop
                    }
                }
                solutionPath.removeLast()
            } else break
        }
        drawMaze(true)
    }

    fun loadMaze(fileName: String): Boolean {
        try {
            val f = File(fileName)
            val lines = f.readLines()
            maze = lines.map { row -> row.split(" ").map { it[0] }.toMutableList() }
            return true
        } catch (_: FileNotFoundException) {
            println("The file $fileName does not exist")
        } catch (_: Exception) {
            println("Cannot load the maze. It has an invalid format")
        }
        return false
    }

    fun saveMaze(fileName: String) {
        val f = File(fileName)
        val data = buildString {
            maze.map { row ->
                appendLine(row.joinToString(" "))
            }
        }
        f.writeText(data)
    }

    fun drawMaze(showSolution: Boolean = false) {
        maze.forEachIndexed() { y, row ->
            row.forEachIndexed { x, it ->
                if (showSolution && Point(x, y) in solutionPath) print("//")
                else if (it == 'p') print("  ") else print("██")
            }
            println()
        }
    }
}

fun main() {
    var generated = false
    val game = GameMazeRunner()
    while (true) {
        println("=== Menu ===")
        println("1. Generate a new maze")
        println("2. Load a maze")
        if (generated) {
            println("3. Save the maze")
            println("4. Display the maze")
            println("5. Find the escape")
        }
        println("0. Exit")
        when (readln().toIntOrNull()) {
            1 -> {
                println("Enter the size of a new maze")
                game.initialize(readln().toInt())
                game.drawMaze()
                generated = true
            }

            2 -> generated = game.loadMaze(readln())

            3 -> if (generated) game.saveMaze(readln())

            4 -> if (generated) game.drawMaze()

            5 -> if (generated) game.solveMaze()

            0 -> break

            else -> println("Incorrect option. Please try again")
        }
    }
    println("Bye!")
}