package com.tgirard12.gradlestepreleaseplugin

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec


class StepTest : WordSpec() {

    init {
        "OtherTask taskName" should {
            "clean" {
                OtherTask("clean").taskName() shouldBe "clean"
            }
            ":clean" {
                OtherTask(":clean").taskName() shouldBe "clean"
            }
            "root:clean" {
                OtherTask("root:clean").taskName() shouldBe "clean"
            }
            ":root:clean" {
                OtherTask(":root:clean").taskName() shouldBe "clean"
            }
        }
        "OtherTask projectName" should {
            "clean" {
                OtherTask("clean").projectName() shouldBe null
            }
            ":clean" {
                OtherTask(":clean").projectName() shouldBe null
            }
            "root:clean" {
                OtherTask("root:clean").projectName() shouldBe "root"
            }
            ":root:clean" {
                OtherTask(":root:clean").projectName() shouldBe "root"
            }
        }
    }
}