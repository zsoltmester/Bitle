plugins {
    id("com.android.application") version "7.2.0" apply false
    id("com.android.library") version "7.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.6.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

open class GenerateAvailableBitleEquations : DefaultTask() {
    @Input
    var outputFilePath: String? = null

    @TaskAction
    fun generateAvailableBitleEquations() {
        val equations = generateEquations()
        File(outputFilePath).printWriter().use { out ->
            equations.forEach {
                out.println(it)
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun generateEquations(): List<String> {
        val availableBitleEquations: MutableList<String> = emptyList<String>().toMutableList()

        // TODO: Refactor to improve the readability of the code.

        for (i in 0u..255u) {
            for (j in 0u..255u) {
                val resultWithAndOperator = (i.toUByte() and j.toUByte()).toString()
                val equationWithAndOperator = "$i&$j=$resultWithAndOperator"
                if (equationWithAndOperator.length == 8) {
                    availableBitleEquations.add(equationWithAndOperator)
                }

                val resultWithOrOperator = (i.toUByte() or j.toUByte()).toString()
                val equationWithOrOperator = "$i|$j=$resultWithOrOperator"
                if (equationWithOrOperator.length == 8) {
                    availableBitleEquations.add(equationWithOrOperator)
                }

                val resultWithXorOperator = (i.toUByte() xor j.toUByte()).toString()
                val equationWithXorOperator = "$i^$j=$resultWithXorOperator"
                if (equationWithXorOperator.length == 8) {
                    availableBitleEquations.add(equationWithXorOperator)
                }
            }
        }

        for (i in 0u..255u) {
            for (j in 0u..255u) {
                for (k in 0u..255u) {
                    val result1 = (i.toUByte() and j.toUByte() and k.toUByte()).toString()
                    val equation1 = "$i&$j&$k=$result1"
                    if (equation1.length == 8) {
                        availableBitleEquations.add(equation1)
                    }

                    val result2 = (i.toUByte() and j.toUByte() or k.toUByte()).toString()
                    val equation2 = "$i&$j|$k=$result2"
                    if (equation2.length == 8) {
                        availableBitleEquations.add(equation2)
                    }

                    val result3 = (i.toUByte() and j.toUByte() xor k.toUByte()).toString()
                    val equation3 = "$i&$j^$k=$result3"
                    if (equation3.length == 8) {
                        availableBitleEquations.add(equation3)
                    }

                    val result4 = (i.toUByte() or j.toUByte() and k.toUByte()).toString()
                    val equation4 = "$i|$j&$k=$result4"
                    if (equation4.length == 8) {
                        availableBitleEquations.add(equation4)
                    }

                    val result5 = (i.toUByte() or j.toUByte() or k.toUByte()).toString()
                    val equation5 = "$i|$j|$k=$result5"
                    if (equation5.length == 8) {
                        availableBitleEquations.add(equation5)
                    }

                    val result6 = (i.toUByte() or j.toUByte() xor k.toUByte()).toString()
                    val equation6 = "$i|$j^$k=$result6"
                    if (equation6.length == 8) {
                        availableBitleEquations.add(equation6)
                    }

                    val result7 = (i.toUByte() xor j.toUByte() and k.toUByte()).toString()
                    val equation7 = "$i^$j&$k=$result7"
                    if (equation7.length == 8) {
                        availableBitleEquations.add(equation7)
                    }

                    val result8 = (i.toUByte() xor j.toUByte() or k.toUByte()).toString()
                    val equation8 = "$i^$j|$k=$result8"
                    if (equation8.length == 8) {
                        availableBitleEquations.add(equation8)
                    }

                    val result9 = (i.toUByte() xor j.toUByte() xor k.toUByte()).toString()
                    val equation9 = "$i^$j^$k=$result9"
                    if (equation9.length == 8) {
                        availableBitleEquations.add(equation9)
                    }
                }
            }
        }

        return availableBitleEquations
    }
}

tasks.register<GenerateAvailableBitleEquations>("generateAvailableBitleEquations") {
    outputFilePath = rootDir.path + "/app/src/main/res/raw/available_bitle_equations"
}