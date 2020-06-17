class NeuralNetwork {
  ArrayList<Matrix> weights=new ArrayList<Matrix>();
  ArrayList<Matrix> biases=new ArrayList<Matrix>();
  ArrayList<Matrix> perceptrons=new ArrayList<Matrix>();
  NeuralNetwork(int [] layers) {
    for (int i=0; i<layers.length-1; i++) {
      int row=layers[i+1];
      int column=layers[i];
      weights.add(new Matrix(row, column));

      row=layers[i+1];
      column=1;
      biases.add(new Matrix(row, column));
    }
  }

  NeuralNetwork copy() {
    int [] parameter = {0};
    NeuralNetwork clone = new NeuralNetwork(parameter);
    clone.weights.clear();
    clone.biases.clear();
    for (int i = 0; i < weights.size(); i++) {
      clone.weights.add(weights.get(i).copy());
      clone.biases.add(biases.get(i).copy());
    }
    return clone;
  }

  void mutate(float mutationRate) {
    for (Matrix weight : weights)
      weight.mutate(mutationRate);
    for (Matrix bias : biases)
      bias.mutate(mutationRate);
  }

  float [] feedforward(float [] input) {
    perceptrons.clear();
    Matrix output=new Matrix(input);
    output.T();
    perceptrons.add(output.copy());
    for (int i=0; i<weights.size(); i++) {
      output=Matrix.mult(weights.get(i), output);

      output=Matrix.add(output, biases.get(i));

      if (i < weights.size() - 1) {
        output=Matrix.sigmoid(output);
        perceptrons.add(output.copy());
      }
    }

    // output = Matrix.softmax(output);
    perceptrons.add(output.copy());
    output.T();
    return output.array[0];
  }

  void train(float [] inputArray, float [] targetArray) {
    float learningRate=0.5;
    ArrayList<Matrix> neurons=new ArrayList<Matrix>();
    ArrayList<Matrix> errors=new ArrayList<Matrix>();

    Matrix target=new Matrix(targetArray);
    target.T();
    Matrix output=new Matrix(inputArray);
    output.T();
    neurons.add(output.copy());

    for (int i=0; i<weights.size(); i++) {
      output=Matrix.mult(weights.get(i), output);
      output=Matrix.add(output, biases.get(i));

      if (i < weights.size() - 1) {
        output=Matrix.sigmoid(output);
        neurons.add(output.copy());
      }
    }

    // output = Matrix.softmax(output);
    neurons.add(output.copy());
    errors.add(Matrix.sub(target, output));

    for (int i=weights.size()-1; i>0; i--) {
      Matrix transposedWeight=Matrix.getT(weights.get(i));
      for (int j=0; j<transposedWeight.column; j++) {
        float sumOfColumn = 0;
        for (int k=0; k<transposedWeight.row; k++) {
          sumOfColumn += abs(transposedWeight.array[k][j]);
        }
        for (int k=0; k<transposedWeight.row; k++) {
          if (sumOfColumn>=1)
            transposedWeight.array[k][j]*=(1/sumOfColumn);
        }
      }

      Matrix error=Matrix.mult(transposedWeight, errors.get(0));
      errors.add(0, error);
    }

    for (int i=weights.size()-1; i>=0; i--) {
      Matrix gradient = errors.get(i).copy();

      if (i < weights.size() - 1) {
        Matrix derivatedSigmoid=neurons.get(i+1).copy();
        Matrix inverseMatrix=derivatedSigmoid.copy();
        inverseMatrix.set(1);
        inverseMatrix=Matrix.sub(inverseMatrix, derivatedSigmoid);
        derivatedSigmoid=Matrix.hadamartProduct(derivatedSigmoid, inverseMatrix);
        gradient=Matrix.hadamartProduct(errors.get(i), derivatedSigmoid);
      }

      Matrix slope=Matrix.mult(gradient, Matrix.getT(neurons.get(i)));
      slope.mult(learningRate);

      Matrix weight=weights.get(i).copy();
      weights.remove(i);
      weights.add(i, Matrix.add(weight, slope));

      Matrix bias=biases.get(i).copy();
      biases.remove(i);
      biases.add(i, Matrix.add(bias, gradient));
    }
  }
}

// float scroll = 0;

// class NetBoard {
//   PGraphics board;
//   int margin = 30;
//   float boardX       = width / 4;
//   int boardSize      = width / 2 + margin;
//   float inputX       = margin / 2;
//   float outputX      = boardSize - margin / 2;
//   float size         = outputX - inputX;
//   float resolution   = size / (layers.length - 1);
//   NetBoard() {
//     board = createGraphics(boardSize, height);
//   }
//   void visualizeNN() {
//     image(board, boardX, 0);
//     board.beginDraw();
//     board.background(255);
//     for ( int i = 0; i < nn.perceptrons.size(); i++) {
//       for ( int j = 0; j < layers[i]; j++) {
//         float x      = inputX + resolution * i;
//         float y      = 50 + 40 * (j * 2  + 1 - layers[i]) / 2 + scroll;
//         float value  = nn.perceptrons.get(i).get(j, 0) ;
//         if (i < nn.weights.size()) {
//           for (int k = 0; k < layers[i + 1]; k++) {
//             float xo = inputX + resolution * (i + 1);
//             float yo = 50 + 40 * (k * 2  + 1 - layers[i + 1]) / 2 + scroll;
//             float w  = nn.weights.get(i).get(k, j);
//             if (w>0)
//               board.stroke(0, 255, 0, 128 * abs(w));
//             else
//               board.stroke(255, 0, 0, 128 * abs(w));
//             board.line(x, y, xo, yo);
//           }
//         }
//         board.stroke(0);
//         board.fill(100 + 155 * value);
//         board.ellipse(x, y, 30, 30);
//         board.textAlign(CENTER);
//         if (value > 0.5)
//           board.fill(0);
//         else
//           board.fill(255);
//         board.text(nf(value, 1, 2), x + 1, y + 5);
//       }
//     }
//     board.endDraw();
//     if (mousePressed) {
//       scroll += mouseY- pmouseY;
//     }
//   }
// }
