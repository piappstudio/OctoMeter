import com.rwmobi.kunigami.domain.extensions.roundToNearestEvenHundredth
import com.rwmobi.kunigami.domain.extensions.roundToTwoDecimalPlaces
import io.kotest.matchers.doubles.shouldBeExactly
import kotlin.test.Test

class DoubleExtensionsKtTest {

    @Test
    fun `roundToNearestEvenHundredth should round 0_015 to 0_02`() {
        val input = 0.015
        val expected = 0.02
        val result = input.roundToNearestEvenHundredth()
        result shouldBeExactly expected
    }

    @Test
    fun `roundToNearestEvenHundredth should round 0_025 to 0_02`() {
        val input = 0.025
        val expected = 0.02
        val result = input.roundToNearestEvenHundredth()
        result shouldBeExactly expected
    }

    @Test
    fun `roundToNearestEvenHundredth should round 0_045 to 0_04`() {
        val input = 0.045
        val expected = 0.04
        val result = input.roundToNearestEvenHundredth()
        result shouldBeExactly expected
    }

    @Test
    fun `roundToNearestEvenHundredth should round 0_055 to 0_06`() {
        val input = 0.055
        val expected = 0.06
        val result = input.roundToNearestEvenHundredth()
        result shouldBeExactly expected
    }

    @Test
    fun `roundToTwoDecimalPlaces should round 0_1234 to 0_12`() {
        val input = 0.1234
        val expected = 0.12
        val result = input.roundToTwoDecimalPlaces()
        result shouldBeExactly expected
    }

    @Test
    fun `roundToTwoDecimalPlaces should round 0_1265 to 0_13`() {
        val input = 0.1265
        val expected = 0.13
        val result = input.roundToTwoDecimalPlaces()
        result shouldBeExactly expected
    }

    @Test
    fun `roundToTwoDecimalPlaces should round 0_1245 to 0_12`() {
        val input = 0.1245
        val expected = 0.12
        val result = input.roundToTwoDecimalPlaces()
        result shouldBeExactly expected
    }

    @Test
    fun `roundToTwoDecimalPlaces should round 0_1255 to 0_13`() {
        val input = 0.1255
        val expected = 0.13
        val result = input.roundToTwoDecimalPlaces()
        result shouldBeExactly expected
    }
}
