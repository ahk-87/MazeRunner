package mazerunner

import java.awt.Point
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.ArrayDeque

class GameMazeRunner() {
    var maze = emptyList<MutableList<Char>>()
    var solutionPath = ArrayDeque<Point>()
    var width = 10
    var height = 10
    val entrance = Point(0, 0)

    fun initialize(dim: Int) {
        width = dim
        height = dim
        maze = List(height) { MutableList(width) { 'w' } }
        generateMaze()
    }

    fun generateMaze() {
        val frontier = mutableSetOf<Point>()
        val oldFrontier = mutableSetOf<Point>()
        val startX = Random().nextInt(1, width - 1)
        val startY = Random().nextInt(1, height - 1)
        maze[startY][startX] = 'p'
        frontier.addAll(Point(startX, startY).getFrontier())

        while (frontier.isNotEmpty()) {
            val randomPoint = frontier.random()
            frontier.remove(randomPoint)
            val neighbors = randomPoint.getFrontier(true)
            if (neighbors.isNotEmpty()) {
                connect(randomPoint, neighbors.random())
            }
            if (neighbors.size < 2)
                if (!oldFrontier.add(randomPoint)) continue
            frontier.addAll(randomPoint.getFrontier())
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

    // neighbor = false: Get the frontier cells - surroundings cell at a distance of 2 and a wall
    // neighbor = true: Get the surrounding neighbours at a distance of 2 and a passage
    // oneCell = true: Get the surrounding cells at a distance of 1
    private fun Point.getFrontier(neighbor: Boolean = false, oneCell: Boolean = false): List<Point> {
        val directions = if (oneCell)
            listOf(Point(1, 0), Point(0, 1), Point(-1, 0), Point(0, -1))
        else
            listOf(Point(2, 0), Point(0, 2), Point(-2, 0), Point(0, -2))
        return directions.map { Point(this.x + it.x, this.y + it.y) }
            .filter { it.x in 0 until width && it.y in 0 until height && (maze[it.y][it.x] == 'p') xor !neighbor }
    }

    fun solveMaze() {
        entrance.y = maze.indexOfFirst { it[1] == 'p' }
        val visited = mutableSetOf<Point>()
        solutionPath.clear()
        solutionPath.addLast(entrance)
        var node: Point

        loop@ while (true) {
            node = solutionPath.last()
            visited.add(node)
            if (node.x != width - 1) {
                val neighbors = node.getFrontier(neighbor = true, oneCell = true)
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

    fun loadMaze(f: File) {
        val lines = f.readLines()
        val h = lines.size
        val w = lines[0].split(" ").size
        maze = lines.map { row -> row.split(" ").map { it[0] }.toMutableList() }
        width = w; height = h
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

            2 -> {
                val file = File(readln())
                try {
                    game.loadMaze(file)
                    generated = true
                } catch (_: FileNotFoundException) {
                    println("The file ${file.name} does not exist")
                } catch (_: Exception) {
                    println("Cannot load the maze. It has an invalid format")
                }
            }

            3 -> if (generated) game.saveMaze(readln())

            4 -> if (generated) game.drawMaze()

            5 -> if (generated) game.solveMaze()

            0 -> break

            else -> println("Incorrect option. Please try again")
        }
    }
    println("Bye!")
}